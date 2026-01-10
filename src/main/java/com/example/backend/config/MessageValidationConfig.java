package com.example.backend.config;

import com.example.backend.constants.MessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 消息验证配置
 * 在应用启动时验证所有消息键是否存在
 */
@Configuration
public class MessageValidationConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MessageValidationConfig.class);

    @Autowired
    private MessageSource messageSource;

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,  // zh_CN
            Locale.JAPANESE,            // ja
            Locale.US                   // en_US
    );

    @Override
    public void run(String... args) {
        log.info("开始验证国际化消息键...");
        
        try {
            // 1. 从 MessageKeys 获取所有消息键
            Set<String> messageKeys = getAllMessageKeys();
            log.info("从 MessageKeys 获取到 {} 个消息键", messageKeys.size());
            
            // 2. 验证每个语言环境的消息键
            Map<String, List<String>> missingKeys = new HashMap<>();
            
            for (Locale locale : SUPPORTED_LOCALES) {
                List<String> missing = validateKeysForLocale(messageKeys, locale);
                if (!missing.isEmpty()) {
                    missingKeys.put(locale.toString(), missing);
                }
            }
            
            // 3. 报告结果
            if (missingKeys.isEmpty()) {
                log.info("✅ 所有消息键验证通过！");
            } else {
                log.warn("⚠️ 发现缺失的消息键：");
                missingKeys.forEach((locale, keys) -> {
                    log.warn("  语言环境: {}", locale);
                    keys.forEach(key -> log.warn("    - {}", key));
                });
                log.warn("请检查 YAML 文件，确保所有消息键都已定义");
            }
            
            // 4. 检查 YAML 文件中的额外键（不在 MessageKeys 中的）
            checkExtraKeysInYaml(messageKeys);
            
        } catch (Exception e) {
            log.error("验证消息键时发生错误", e);
        }
    }

    /**
     * 通过反射获取 MessageKeys 类中的所有常量值
     */
    private Set<String> getAllMessageKeys() {
        Set<String> keys = new HashSet<>();
        
        try {
            Class<?> messageKeysClass = MessageKeys.class;
            Field[] fields = messageKeysClass.getDeclaredFields();
            
            for (Field field : fields) {
                // 只获取 public static final String 类型的字段
                if (java.lang.reflect.Modifier.isPublic(field.getModifiers()) &&
                    java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    java.lang.reflect.Modifier.isFinal(field.getModifiers()) &&
                    field.getType() == String.class) {
                    
                    field.setAccessible(true);
                    String value = (String) field.get(null);
                    if (value != null && !value.isEmpty()) {
                        keys.add(value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取 MessageKeys 常量时发生错误", e);
        }
        
        return keys;
    }

    /**
     * 验证指定语言环境的消息键
     */
    private List<String> validateKeysForLocale(Set<String> messageKeys, Locale locale) {
        List<String> missingKeys = new ArrayList<>();
        
        for (String key : messageKeys) {
            try {
                String message = messageSource.getMessage(key, null, locale);
                // 如果返回的是键本身，说明找不到消息
                if (message.equals(key)) {
                    missingKeys.add(key);
                }
            } catch (org.springframework.context.NoSuchMessageException e) {
                missingKeys.add(key);
            } catch (Exception e) {
                log.debug("验证消息键 {} 时发生异常: {}", key, e.getMessage());
                missingKeys.add(key);
            }
        }
        
        return missingKeys;
    }

    /**
     * 检查 YAML 文件中是否有额外的键（不在 MessageKeys 中定义的）
     */
    private void checkExtraKeysInYaml(Set<String> messageKeys) {
        log.info("检查 YAML 文件中的额外键...");
        
        for (Locale locale : SUPPORTED_LOCALES) {
            try {
                Set<String> yamlKeys = extractKeysFromYaml(locale);
                Set<String> extraKeys = new HashSet<>(yamlKeys);
                extraKeys.removeAll(messageKeys);
                
                if (!extraKeys.isEmpty()) {
                    log.warn("⚠️ 语言环境 {} 的 YAML 文件中有 {} 个未在 MessageKeys 中定义的键：", 
                            locale.toString(), extraKeys.size());
                    extraKeys.forEach(key -> log.warn("    - {}", key));
                    log.warn("建议：将这些键添加到 MessageKeys 类中，以保持一致性");
                }
            } catch (Exception e) {
                log.debug("检查语言环境 {} 的 YAML 文件时发生异常: {}", locale, e.getMessage());
            }
        }
    }

    /**
     * 从 YAML 文件中提取所有键（简单实现，只提取顶级键）
     */
    private Set<String> extractKeysFromYaml(Locale locale) {
        Set<String> keys = new HashSet<>();
        
        try {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String lang = language.split(";")[0];
            String yamlFileName = String.format("i18n/messages_%s%s.yml", 
                    lang, country.isEmpty() ? "" : "_" + country);
            
            ClassPathResource resource = new ClassPathResource(yamlFileName);
            if (resource.exists()) {
                // 使用 YamlPropertiesFactoryBean 加载
                org.springframework.beans.factory.config.YamlPropertiesFactoryBean yaml = 
                        new org.springframework.beans.factory.config.YamlPropertiesFactoryBean();
                yaml.setResources(resource);
                Properties properties = yaml.getObject();
                
                if (properties != null) {
                    keys.addAll(properties.stringPropertyNames());
                }
            }
        } catch (Exception e) {
            log.debug("提取 YAML 键时发生异常: {}", e.getMessage());
        }
        
        return keys;
    }
}
