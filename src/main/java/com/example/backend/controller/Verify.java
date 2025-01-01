package com.example.backend.controller;

import cloud.tianai.captcha.application.DefaultImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaProperties;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.LocalCacheStore;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.generator.ImageTransform;
import cloud.tianai.captcha.generator.common.model.dto.GenerateParam;
import cloud.tianai.captcha.generator.common.model.dto.ImageCaptchaInfo;
import cloud.tianai.captcha.generator.impl.MultiImageCaptchaGenerator;
import cloud.tianai.captcha.generator.impl.transform.Base64ImageTransform;
import cloud.tianai.captcha.interceptor.CaptchaInterceptorGroup;
import cloud.tianai.captcha.interceptor.impl.BasicTrackCaptchaInterceptor;
import cloud.tianai.captcha.interceptor.impl.ParamCheckCaptchaInterceptor;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.validator.ImageCaptchaValidator;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cloud.tianai.captcha.validator.impl.SimpleImageCaptchaValidator;
import cn.hutool.core.lang.hash.Hash;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.RedisType;
import com.example.backend.utils.MessageUtils;
import com.fasterxml.uuid.Generators;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
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
@RestController
public class Verify {
  @Autowired
  private ImageCaptchaApplication imageCaptchaApplication;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping("/api/verify/sendCode")
  public RestBean<HashMap> sendMail(@RequestBody @Validated SendEmailQuery query) {
    // 使用Hutool发送邮件
    SecureRandom secureRandom = new SecureRandom();
    int code = 100000 + secureRandom.nextInt(900000);

    String to = query.getEmail();
    String subject = MessageUtils.getMessage("email.verifyCode.title");
    String content = MessageUtils.getMessage("email.verifyCode.content", String.valueOf(code));

    String uuid = Generators.timeBasedEpochGenerator().generate().toString().replace("-", "");
    String key = RedisType.verifyCode.getCode() + ":" + uuid;

    redisTemplate.opsForValue().set(key, String.valueOf(code), 60 * 5, TimeUnit.SECONDS);

    MailUtil.send(to, subject, content, false);

    HashMap map = new HashMap<>();

    map.put("token", uuid);

    return RestBean.success(map, MessageUtils.getMessage("success.send"));
  }
  @PostMapping("/api/verify/captcha")
  public CaptchaResponse<ImageCaptchaVO>  verify () {
    ImageCaptchaApplication application = createImageCaptchaApplication();
    // 生成验证码数据， 可以将该数据直接返回给前端 ， 可配合 tianai-captcha-web-sdk 使用
    // 支持生成 滑动验证码(SLIDER)、旋转验证码(ROTATE)、滑动还原验证码(CONCAT)、文字点选验证码(WORD_IMAGE_CLICK)
    CaptchaResponse<ImageCaptchaVO> res = application.generateCaptcha("SLIDER");
    System.out.println(res);

    // 校验验证码， ImageCaptchaTrack 和 id 均为前端传开的参数， 可将 valid数据直接返回给 前端
    // 注意: 该项目只负责生成和校验验证码数据， 至于二次验证等需要自行扩展
//    String id =res.getId();
//    ImageCaptchaTrack imageCaptchaTrack = null;
//    ApiResponse<?> valid = application.matching(id, imageCaptchaTrack);
//    System.out.println(valid.isSuccess());

    return res;
  }
  @PostMapping("/api/verify/checkCaptcha")
  public ApiResponse<?> checkCaptcha(@RequestBody CaptchaData data,
                                     HttpServletRequest request) {
    ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(), data.getData());
    if (response.isSuccess()) {
      return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
    }
    return response;
  }
  public static ImageCaptchaApplication createImageCaptchaApplication() {
    // 验证码资源管理器 该类负责管理验证码背景图和模板图等数据
    ImageCaptchaResourceManager imageCaptchaResourceManager = new DefaultImageCaptchaResourceManager();
    // 验证码生成器； 注意: 生成器必须调用init(...)初始化方法 true为加载默认资源，false为不加载，
    ImageCaptchaGenerator generator = new MultiImageCaptchaGenerator(imageCaptchaResourceManager).init(true);
    // 验证码校验器
    ImageCaptchaValidator imageCaptchaValidator = new SimpleImageCaptchaValidator();
    // 缓存, 用于存放校验数据
    CacheStore cacheStore = new LocalCacheStore();
    // 验证码拦截器， 可以是单个，也可以是一组拦截器，可以嵌套， 这里演示加载参数校验拦截，和 滑动轨迹拦截
    CaptchaInterceptorGroup group = new CaptchaInterceptorGroup();
    group.addInterceptor(new ParamCheckCaptchaInterceptor());
    group.addInterceptor(new BasicTrackCaptchaInterceptor());

    ImageCaptchaProperties prop = new ImageCaptchaProperties();
    // application 验证码封装， prop为所需的一些扩展参数
    ImageCaptchaApplication application = new DefaultImageCaptchaApplication(generator, imageCaptchaValidator, cacheStore, prop, group);
    return application;
  }
}
