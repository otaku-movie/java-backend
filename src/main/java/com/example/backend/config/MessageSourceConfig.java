package com.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国际化消息源配置
 * 优化：添加缓存机制，避免重复加载 YAML 文件
 */
@Configuration
public class MessageSourceConfig {

  private static final Logger log = LoggerFactory.getLogger(MessageSourceConfig.class);

  @Bean
  public MessageSource messageSource() {
    return new YamlMessageSource();
  }

  public static class YamlMessageSource extends AbstractMessageSource {

    private static final String BASENAME = "i18n/messages";
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 语言 → 实际存在的资源文件后缀映射。
     * 资源文件命名并不统一（messages_ja.yml / messages_zh_CN.yml / messages_en_US.yml），
     * 而客户端传来的 Accept-Language 可能是 zh、zh-CN、zh-（甚至畸形）、ja-JP 等多种形态。
     * 这里只按「语言」归一化到固定文件，彻底规避 country 带不带、带得对不对导致找不到文件、
     * 回退默认语言的问题（这正是 App 切换语言后字典不变的根因）。
     */
    private static final Map<String, String> LANG_TO_FILE_SUFFIX = Map.of(
        "zh", "zh_CN",
        "ja", "ja",
        "en", "en_US"
    );
    
    // 缓存已加载的 Properties，key 为 locale.toString()
    private static final Map<String, Properties> propertiesCache = new ConcurrentHashMap<>();
    
    // 缓存已解析的 MessageFormat，key 为 code + "_" + locale.toString()
    private static final Map<String, MessageFormat> messageFormatCache = new ConcurrentHashMap<>();

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
      // 先检查缓存
      String cacheKey = code + "_" + locale.toString();
      MessageFormat cachedFormat = messageFormatCache.get(cacheKey);
      if (cachedFormat != null) {
        return cachedFormat;
      }
      
      // 加载 Properties（带缓存）
      Properties properties = loadYmlByLocale(locale);
      String message = properties.getProperty(code);
      
      if (message == null) {
        // 如果当前语言找不到，回退到默认语言
        Locale defaultLocale = Locale.getDefault();
        if (!defaultLocale.equals(locale)) {
          properties = loadYmlByLocale(defaultLocale);
          message = properties.getProperty(code);
        }
        
        if (message == null) {
          // 如果仍然找不到，记录警告并返回代码本身
          log.warn("Message key '{}' not found for locale '{}'", code, locale);
          message = code;
        }
      }
      
      MessageFormat format = new MessageFormat(message, locale);
      
      // 缓存结果
      messageFormatCache.put(cacheKey, format);
      
      return format;
    }

    /**
     * 加载指定语言环境的 YAML 文件（带缓存）
     */
    private Properties loadYmlByLocale(Locale locale) {
      String localeKey = locale.toString();
      
      // 检查缓存
      Properties cached = propertiesCache.get(localeKey);
      if (cached != null) {
        return cached;
      }
      
      // 构建文件名：优先按「语言」归一化到实际存在的文件后缀（zh→zh_CN、ja→ja、en→en_US）；
      // 未在映射表中的语言才退回「语言[_国家]」的原始拼法，保持向后兼容。
      String language = locale.getLanguage();
      String country = locale.getCountry();
      String lang = language.split(";")[0].toLowerCase();
      String suffix = LANG_TO_FILE_SUFFIX.get(lang);
      if (suffix == null) {
        suffix = country.isEmpty() ? lang : lang + "_" + country;
      }
      String yamlFileName = String.format("%s_%s.yml", BASENAME, suffix);
      
      log.debug("Loading YAML file: {}", yamlFileName);
      
      ClassPathResource yamlFileResource = new ClassPathResource(yamlFileName);
      
      if (!yamlFileResource.exists()) {
        log.warn("YAML file not found: {}, falling back to default locale", yamlFileName);
        // 如果文件不存在，尝试加载默认语言
        if (!locale.equals(Locale.getDefault())) {
          return loadYmlByLocale(Locale.getDefault());
        }
        // 如果默认语言也没有，返回空的 Properties
        Properties empty = new Properties();
        propertiesCache.put(localeKey, empty);
        return empty;
      }
      
      try {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(yamlFileResource);
        Properties properties = yaml.getObject();
        
        if (properties == null) {
          properties = new Properties();
        }
        
        // 缓存结果
        propertiesCache.put(localeKey, properties);
        
        log.info("Loaded {} messages for locale: {}", properties.size(), localeKey);
        
        return properties;
      } catch (Exception e) {
        log.error("Error loading YAML file: {}", yamlFileName, e);
        Properties empty = new Properties();
        propertiesCache.put(localeKey, empty);
        return empty;
      }
    }
    
    /**
     * 清除缓存（用于开发环境热重载）
     */
    public static void clearCache() {
      propertiesCache.clear();
      messageFormatCache.clear();
      log.info("Message source cache cleared");
    }
  }
}
