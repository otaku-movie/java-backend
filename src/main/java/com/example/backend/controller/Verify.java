package com.example.backend.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cn.hutool.extra.mail.MailUtil;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.service.EmailTemplateService;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.RedisType;
import com.example.backend.utils.MessageUtils;
import com.fasterxml.uuid.Generators;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
  RedisTemplate redisTemplate;

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

    MailUtil.send(to, subject, htmlContent, true);

    return RestBean.success(Map.of("token", uuid), MessageUtils.getMessage(MessageKeys.Success.SEND));
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
