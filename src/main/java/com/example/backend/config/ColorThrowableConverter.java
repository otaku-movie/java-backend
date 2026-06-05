package com.example.backend.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 按日志级别给异常堆栈整段染色的转换器。
 *
 * <p>继承 {@link ThrowableProxyConverter}（它本身是 logback 识别的「异常处理器」），
 * 所以放在 pattern 顶层时，logback 不会再自动在外层追加一段未染色的堆栈，
 * 从而避免堆栈重复打印；同时这里把堆栈整段包上 ANSI 颜色。
 *
 * <ul>
 *   <li>ERROR：加粗亮红（与 {@code %boldRed} 的标题行一致）</li>
 *   <li>WARN：黄色</li>
 *   <li>其它级别：不染色</li>
 * </ul>
 *
 * <p>在 logback-spring.xml 中通过
 * {@code <conversionRule conversionWord="cex" converterClass="..."/>}
 * 注册后，用 {@code %cex} 代替 {@code %throwable} 即可。
 */
public class ColorThrowableConverter extends ThrowableProxyConverter {

  private static final String BOLD_BRIGHT_RED = "\u001b[1;91m";
  private static final String YELLOW = "\u001b[33m";
  private static final String RESET = "\u001b[0m";

  @Override
  public String convert(ILoggingEvent event) {
    String body = super.convert(event);
    if (body == null || body.isEmpty()) {
      return body;
    }
    Level level = event.getLevel();
    if (level == Level.ERROR) {
      return BOLD_BRIGHT_RED + body + RESET;
    }
    if (level == Level.WARN) {
      return YELLOW + body + RESET;
    }
    return body;
  }
}
