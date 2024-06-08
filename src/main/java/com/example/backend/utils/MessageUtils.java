package com.example.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {

  private static MessageSource messageSource;

  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    MessageUtils.messageSource = messageSource;
  }

  /**
   * 获取国际化消息
   * @param code 消息键
   * @param args 参数
   * @return 获取国际化翻译值
   */
  public static String getMessage(String code, Object... args) {
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }

  /**
   * 根据路径和属性获取国际化消息
   * @param path 路径
   * @param property 属性
   * @param args 参数
   * @return 获取国际化翻译值
   */
  public static String getMessageWithProperty(String path, String property, Object... args) {
    String code = resolveCodeWithProperty(path, property);
    return getMessage(code, args);
  }

  /**
   * 解析消息键，包括自定义属性
   * @param path 路径
   * @param property 属性
   * @return 解析后的消息键
   */
  private static String resolveCodeWithProperty(String path, String property) {
    return path + "." + property;
  }
}
