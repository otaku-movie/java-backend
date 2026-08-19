package com.example.backend.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.service.EmailTemplateService;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.exception.BusinessException;
import com.example.backend.utils.MessageUtils;
import com.fasterxml.uuid.Generators;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
class CaptchaData {
  private String  id;
  private ImageCaptchaTrack data;
}

@Data
class SendEmailQuery {
  @NotEmpty(message = "{validator.saveUser.email.required}")
  @Email(message = "{validator.saveUser.email.required}")
  String email;
}
@Slf4j
@RestController
public class Verify {
  @Autowired
  private ImageCaptchaApplication imageCaptchaApplication;

  @Autowired
  private EmailTemplateService emailTemplateService;

  @Resource
  private RedisTemplate<String, String> redisTemplate;

  @Value("${app.mail.host}")
  private String mailHost;

  @Value("${app.mail.port}")
  private Integer mailPort;

  @Value("${app.mail.from}")
  private String mailFrom;

  @Value("${app.mail.user}")
  private String mailUser;

  @Value("${app.mail.pass}")
  private String mailPass;

  @Value("${app.mail.ssl-enable}")
  private Boolean mailSslEnable;

  /**
   * 邮件模板预览 - 浏览器直接打开可查看效果
   * @param code 示例验证码，默认 123456
   */
  @GetMapping(value = ApiPaths.Verify.EMAIL_PREVIEW, produces = MediaType.TEXT_HTML_VALUE)
  public String emailPreview(@RequestParam(defaultValue = "123456") String code) {
    return emailTemplateService.getVerifyCodeHtml(code);
  }

  @PostMapping(ApiPaths.Verify.SEND_CODE)
  public RestBean<Map<String, Object>> sendMail(@RequestBody @Validated SendEmailQuery query) {
    SecureRandom secureRandom = new SecureRandom();
    int code = 100000 + secureRandom.nextInt(900000);

    String to = query.getEmail();
    String subject = MessageUtils.getMessage(MessageKeys.Common.User.VerifyCode.TITLE);
    String htmlContent = emailTemplateService.getVerifyCodeHtml(String.valueOf(code));

    String uuid = Generators.timeBasedEpochGenerator().generate().toString().replace("-", "");
    String key = RedisType.verifyCode.getCode() + ":" + uuid;

    redisTemplate.opsForValue().set(key, String.valueOf(code), 60 * 5, TimeUnit.SECONDS);

    log.info("发送验证码邮件: to={}", maskEmail(to));
    try {
      MailUtil.send(createMailAccount(), to, subject, htmlContent, true);
    } catch (Exception e) {
      redisTemplate.delete(key);
      log.error("验证码邮件发送失败: to={}", maskEmail(to), e);
      throw new BusinessException(ResponseCode.ERROR, MessageKeys.Error.SEND_FAILED, e);
    }

    return RestBean.success(Map.of("token", uuid), MessageUtils.getMessage(MessageKeys.Success.SEND));
  }

  private static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }
    int at = email.indexOf('@');
    String local = email.substring(0, at);
    String domain = email.substring(at);
    if (local.length() <= 2) {
      return local.charAt(0) + "***" + domain;
    }
    return local.substring(0, 2) + "***" + domain;
  }

  private MailAccount createMailAccount() {
    MailAccount account = new MailAccount();
    account.setHost(mailHost);
    account.setPort(mailPort);
    account.setFrom(mailFrom);
    account.setUser(mailUser);
    account.setPass(mailPass);
    account.setAuth(true);
    account.setSslEnable(mailSslEnable);
    return account;
  }

  @PostMapping(ApiPaths.Verify.CAPTCHA)
  public CaptchaResponse<ImageCaptchaVO> verify() {
    CaptchaResponse<ImageCaptchaVO> res = imageCaptchaApplication.generateCaptcha("SLIDER");
    log.debug("生成验证码: id={}", res.getId());
    return res;
  }

  @PostMapping(ApiPaths.Verify.CHECK_CAPTCHA)
  public ApiResponse<?> checkCaptcha(@RequestBody CaptchaData data) {
    ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(), data.getData());
    if (response.isSuccess()) {
      return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
    }
    return response;
  }
}
