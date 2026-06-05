package com.example.backend.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * logback 自定义转换器：把包裹的内容用「加粗亮红」整段染色。
 *
 * <p>内置 {@code %clr} 的 {@code red} 是普通暗红，深色终端里偏闷；这里用
 * ANSI 加粗(1) + 亮红(91) 让 ERROR 更醒目。ESC 控制字符用 Java 的
 * {@code \u001b} 输出，避免在 logback XML 里写控制字符（XML 1.0 不允许）。
 *
 * <p>在 logback-spring.xml 中通过
 * {@code <conversionRule conversionWord="boldRed" converterClass="..."/>}
 * 注册后，使用 {@code %boldRed(...)} 即可。
 */
public class BoldRedLogConverter extends CompositeConverter<ILoggingEvent> {

  private static final String BOLD_BRIGHT_RED = "\u001b[1;91m";
  private static final String RESET = "\u001b[0m";

  @Override
  protected String transform(ILoggingEvent event, String in) {
    return BOLD_BRIGHT_RED + in + RESET;
  }
}
