package com.example.backend.utils;

import com.example.backend.constants.MessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 消息键一致性检查工具
 * 用于检查 MessageKeys 类和 YAML 文件的一致性
 * 
 * 使用方法：
 * MessageKeysChecker.check(); // 在开发环境手动调用
 */
public class MessageKeysChecker {

    private static final Logger log = LoggerFactory.getLogger(MessageKeysChecker.class);

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,  // zh_CN
            Locale.JAPANESE,            // ja
            Locale.US                   // en_US
    );

    /**
     * 执行完整的一致性检查
     */
    public static void check() {
        log.info("开始检查消息键一致性...");
        
        try {
            // 1. 获取 MessageKeys 中的所有键
            Set<String> messageKeys = getAllMessageKeys();
            log.info("MessageKeys 中定义了 {} 个消息键", messageKeys.size());
            
            // 2. 检查每个语言环境
            for (Locale locale : SUPPORTED_LOCALES) {
                checkLocale(locale, messageKeys);
            }
            
            log.info("✅ 一致性检查完成");
        } catch (Exception e) {
            log.error("检查消息键一致性时发生错误", e);
        }
    }

    /**
     * 检查指定语言环境
     */
    private static void checkLocale(Locale locale, Set<String> messageKeys) {
        log.info("检查语言环境: {}", locale);
        
        try {
            // 从 YAML 文件加载键
            Set<String> yamlKeys = loadKeysFromYaml(locale);
            log.info("  YAML 文件中定义了 {} 个键", yamlKeys.size());
            
            // 检查缺失的键
            Set<String> missingInYaml = new HashSet<>(messageKeys);
            missingInYaml.removeAll(yamlKeys);
            
            if (!missingInYaml.isEmpty()) {
                log.warn("  ⚠️ 以下 {} 个键在 MessageKeys 中定义，但在 YAML 文件中缺失：", missingInYaml.size());
                missingInYaml.forEach(key -> log.warn("    - {}", key));
            }
            
            // 检查额外的键
            Set<String> extraInYaml = new HashSet<>(yamlKeys);
            extraInYaml.removeAll(messageKeys);
            
            if (!extraInYaml.isEmpty()) {
                log.warn("  ⚠️ 以下 {} 个键在 YAML 文件中定义，但未在 MessageKeys 中定义：", extraInYaml.size());
                extraInYaml.forEach(key -> log.warn("    - {}", key));
            }
            
            if (missingInYaml.isEmpty() && extraInYaml.isEmpty()) {
                log.info("  ✅ 语言环境 {} 检查通过", locale);
            }
            
        } catch (Exception e) {
            log.error("检查语言环境 {} 时发生错误", locale, e);
        }
    }

    /**
     * 通过反射获取 MessageKeys 类中的所有常量值
     */
    private static Set<String> getAllMessageKeys() {
        Set<String> keys = new HashSet<>();
        
        try {
            Class<?> messageKeysClass = MessageKeys.class;
            Field[] fields = messageKeysClass.getDeclaredFields();
            
            for (Field field : fields) {
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
     * 从 YAML 文件加载所有键
     */
    private static Set<String> loadKeysFromYaml(Locale locale) {
        Set<String> keys = new HashSet<>();
        
        try {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String lang = language.split(";")[0];
            String yamlFileName = String.format("i18n/messages_%s%s.yml", 
                    lang, country.isEmpty() ? "" : "_" + country);
            
            ClassPathResource resource = new ClassPathResource(yamlFileName);
            if (!resource.exists()) {
                log.warn("  YAML 文件不存在: {}", yamlFileName);
                return keys;
            }
            
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(resource);
            Properties properties = yaml.getObject();
            
            if (properties != null) {
                keys.addAll(properties.stringPropertyNames());
            }
        } catch (Exception e) {
            log.error("加载 YAML 文件时发生错误", e);
        }
        
        return keys;
    }

    /**
     * 生成报告
     */
    public static void generateReport() {
        log.info("生成消息键一致性报告...");
        
        try {
            Set<String> messageKeys = getAllMessageKeys();
            
            Map<String, Map<String, Boolean>> report = new LinkedHashMap<>();
            
            for (Locale locale : SUPPORTED_LOCALES) {
                Set<String> yamlKeys = loadKeysFromYaml(locale);
                Map<String, Boolean> localeReport = new LinkedHashMap<>();
                
                for (String key : messageKeys) {
                    localeReport.put(key, yamlKeys.contains(key));
                }
                
                report.put(locale.toString(), localeReport);
            }
            
            // 打印报告
            log.info("=== 消息键一致性报告 ===");
            report.forEach((locale, keys) -> {
                log.info("语言环境: {}", locale);
                long missingCount = keys.values().stream().filter(v -> !v).count();
                if (missingCount > 0) {
                    log.warn("  缺失键数量: {}", missingCount);
                } else {
                    log.info("  ✅ 所有键都存在");
                }
            });
            
        } catch (Exception e) {
            log.error("生成报告时发生错误", e);
        }
    }
}
