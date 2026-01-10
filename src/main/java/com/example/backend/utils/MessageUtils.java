package com.example.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息工具类
 * 
 * 使用示例：
 * 1. 基本用法：
 *    MessageUtils.getMessage(MessageKeys.SUCCESS_SAVE);
 * 
 * 2. 带参数：
 *    MessageUtils.getMessage(MessageKeys.ERROR_REPEAT, MessageUtils.getMessage(MessageKeys.REPEAT_ROLE_NAME));
 * 
 * 3. 使用常量类（推荐）：
 *    MessageUtils.success("save");
 */
@Component
public class MessageUtils {

  private static final Logger log = LoggerFactory.getLogger(MessageUtils.class);
  private static MessageSource messageSource;

  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    MessageUtils.messageSource = messageSource;
  }

  /**
   * 获取国际化消息
   * 
   * @param code 消息键（推荐使用 MessageKeys 常量）
   * @param args 参数
   * @return 国际化翻译值，如果找不到则返回代码本身
   */
  public static String getMessage(String code, Object... args) {
    if (messageSource == null) {
      log.warn("MessageSource not initialized, returning code: {}", code);
      return code;
    }
    
    try {
      Locale locale = LocaleContextHolder.getLocale();
      return messageSource.getMessage(code, args, locale);
    } catch (Exception e) {
      log.warn("Failed to get message for code: {}, error: {}", code, e.getMessage());
      return code;
    }
  }

  /**
   * 获取国际化消息，如果找不到返回默认值
   * 
   * @param code 消息键
   * @param defaultMessage 默认消息
   * @param args 参数
   * @return 国际化翻译值
   */
  public static String getMessage(String code, String defaultMessage, Object... args) {
    if (messageSource == null) {
      return defaultMessage != null ? defaultMessage : code;
    }
    
    try {
      Locale locale = LocaleContextHolder.getLocale();
      return messageSource.getMessage(code, args, defaultMessage, locale);
    } catch (Exception e) {
      log.warn("Failed to get message for code: {}, using default: {}", code, defaultMessage);
      return defaultMessage != null ? defaultMessage : code;
    }
  }

  /**
   * 根据 MessageSourceResolvable 获取消息
   * 
   * @param resolvable 可解析的消息对象
   * @return 国际化翻译值
   */
  public static String getMessage(MessageSourceResolvable resolvable) {
    if (messageSource == null) {
      return resolvable.getDefaultMessage() != null ? resolvable.getDefaultMessage() : "Unknown";
    }
    
    try {
      Locale locale = LocaleContextHolder.getLocale();
      return messageSource.getMessage(resolvable, locale);
    } catch (Exception e) {
      log.warn("Failed to get message from resolvable: {}", resolvable);
      return resolvable.getDefaultMessage() != null ? resolvable.getDefaultMessage() : "Unknown";
    }
  }

  /**
   * 根据路径和属性获取国际化消息
   * 
   * @param path 路径（如 "success"）
   * @param property 属性（如 "save"）
   * @param args 参数
   * @return 国际化翻译值（如 "success.save"）
   */
  public static String getMessageWithProperty(String path, String property, Object... args) {
    String code = resolveCodeWithProperty(path, property);
    return getMessage(code, args);
  }

  /**
   * 便利方法：获取成功消息
   * 
   * @param key 消息键（不带 "success." 前缀）
   * @param args 参数
   * @return 国际化翻译值
   */
  public static String success(String key, Object... args) {
    return getMessageWithProperty("success", key, args);
  }

  /**
   * 便利方法：获取错误消息
   * 
   * @param key 消息键（不带 "error." 前缀）
   * @param args 参数
   * @return 国际化翻译值
   */
  public static String error(String key, Object... args) {
    return getMessageWithProperty("error", key, args);
  }

  /**
   * 便利方法：获取验证器消息
   * 
   * @param path 路径（如 "login.password"）
   * @param key 消息键（如 "required"）
   * @param args 参数
   * @return 国际化翻译值（如 "validator.login.password.required"）
   */
  public static String validator(String path, String key, Object... args) {
    return getMessage("validator." + path + "." + key, args);
  }

  /**
   * 解析消息键，包括自定义属性
   * 
   * @param path 路径
   * @param property 属性
   * @return 解析后的消息键
   */
  private static String resolveCodeWithProperty(String path, String property) {
    return path + "." + property;
  }
}
