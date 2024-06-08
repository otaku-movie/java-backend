package com.example.backend.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

@Component
public class MessageUtils {

  private static MessageSource messageSource;

  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    MessageUtils.messageSource = messageSource;
  }

  /**
   * 根据消息键和参数获取消息，委托给spring messageSource
   * @param code 消息键
   * @param defaultMessage 默认消息
   * @param args 参数
   * @return 获取国际化翻译值
   */
  public static String getMessage(String code, String defaultMessage, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }

  /**
   * 根据路径和属性获取国际化消息
   * @param path 路径
   * @param property 自定义属性
   * @param defaultMessage 默认消息
   * @return 获取国际化翻译值
   */
  public static String getMessageByPathAndProperty(String path, String property, String defaultMessage) {
    String code = "path.message." + path + "." + property;
    return getMessage(code, defaultMessage);
  }

  /**
   * 根据路径获取国际化消息
   * @param path 路径
   * @param defaultMessage 默认消息
   * @return 获取国际化翻译值
   */
  public static String getMessageByPath(String path, String defaultMessage) {
    String code = "path.message." + path;
    return getMessage(code, defaultMessage);
  }

  /**
   * 根据路径获取带有参数的国际化消息
   * @param path 路径
   * @param args 参数
   * @return 获取国际化翻译值
   */
  public static String getMessageByPathWithArgs(String path, Object... args) {
    String code = "path.message." + path;
    return getMessage(code, code, args);
  }
}
