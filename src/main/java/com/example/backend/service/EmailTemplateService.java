package com.example.backend.service;

import com.example.backend.constants.MessageKeys;
import com.example.backend.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 邮件 HTML 模板服务
 */
@Service
public class EmailTemplateService {

  private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

  private static final String TEMPLATE_PATH = "templates/email/verify-code.html";

  /**
   * 获取验证码邮件的 HTML 内容
   *
   * @param code 验证码
   * @return HTML 字符串
   */
  public String getVerifyCodeHtml(String code) {
    String template = loadTemplate();
    if (template == null) {
      return fallbackHtml(code);
    }
    return template
        .replace("{{title}}", MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_TITLE))
        .replace("{{label}}", MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_LABEL))
        .replace("{{code}}", code)
        .replace("{{tip}}", MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_TIP))
        .replace("{{footer}}", MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_FOOTER));
  }

  private String loadTemplate() {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
      if (is == null) {
        log.warn("邮件模板不存在: {}", TEMPLATE_PATH);
        return null;
      }
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.warn("加载邮件模板失败: {}", e.getMessage());
      return null;
    }
  }

  private String fallbackHtml(String code) {
    String title = MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_TITLE);
    String label = MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_LABEL);
    String tip = MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_TIP);
    String footer = MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.HTML_FOOTER);
    return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"></head><body style="font-family:sans-serif;padding:20px">
        <h2>%s</h2><p>%s</p><div style="font-size:28px;color:#6366f1;font-weight:bold;margin:16px 0">%s</div><p style="color:#94a3b8">%s</p><p style="color:#94a3b8;font-size:12px;margin-top:20px">%s</p>
        </body></html>
        """.formatted(title, label, code, tip, footer);
  }
}
