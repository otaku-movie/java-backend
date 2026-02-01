package com.example.backend.config;

import cloud.tianai.captcha.application.DefaultImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaProperties;
import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.LocalCacheStore;
import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.generator.impl.MultiImageCaptchaGenerator;
import cloud.tianai.captcha.interceptor.CaptchaInterceptorGroup;
import cloud.tianai.captcha.interceptor.impl.BasicTrackCaptchaInterceptor;
import cloud.tianai.captcha.interceptor.impl.ParamCheckCaptchaInterceptor;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.validator.ImageCaptchaValidator;
import cloud.tianai.captcha.validator.impl.SimpleImageCaptchaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CaptchaConfig {

  @Primary
  @Bean
  public ImageCaptchaApplication imageCaptchaApplication() {
    ImageCaptchaResourceManager resourceManager = new DefaultImageCaptchaResourceManager();
    ImageCaptchaGenerator generator = new MultiImageCaptchaGenerator(resourceManager).init(true);
    ImageCaptchaValidator validator = new SimpleImageCaptchaValidator();
    CacheStore cacheStore = new LocalCacheStore();

    CaptchaInterceptorGroup group = new CaptchaInterceptorGroup();
    group.addInterceptor(new ParamCheckCaptchaInterceptor());
    group.addInterceptor(new BasicTrackCaptchaInterceptor());

    ImageCaptchaProperties prop = new ImageCaptchaProperties();
    return new DefaultImageCaptchaApplication(generator, validator, cacheStore, prop, group);
  }
}
