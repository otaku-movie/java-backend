package com.example.backend.config;

import cloud.tianai.captcha.application.DefaultImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaProperties;
import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.ConCurrentExpiringMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class CaptchaConfig {
  private static final Logger log = LoggerFactory.getLogger(CaptchaConfig.class);

  @Primary
  @Bean
  public CacheStore captchaCacheStore() {
    return new DisposableLocalCacheStore();
  }

  @Primary
  @Bean
  public ImageCaptchaApplication imageCaptchaApplication(CacheStore captchaCacheStore) {
    ImageCaptchaResourceManager resourceManager = new DefaultImageCaptchaResourceManager();
    ImageCaptchaGenerator generator = new MultiImageCaptchaGenerator(resourceManager).init(true);
    ImageCaptchaValidator validator = new SimpleImageCaptchaValidator();

    CaptchaInterceptorGroup group = new CaptchaInterceptorGroup();
    group.addInterceptor(new ParamCheckCaptchaInterceptor());
    group.addInterceptor(new BasicTrackCaptchaInterceptor());

    ImageCaptchaProperties prop = new ImageCaptchaProperties();
    return new DefaultImageCaptchaApplication(generator, validator, captchaCacheStore, prop, group);
  }

  /**
   * tianai-captcha 的 LocalCacheStore 内部使用 ConCurrentExpiringMap，并创建
   * expiring-map-expire-* 定时线程；该依赖版本没有暴露 close/destroy 方法。
   *
   * <p>把缓存作为 Spring Bean 注册，并在容器销毁时反射关闭内部 ScheduledExecutorService，
   * 避免 DevTools restart 或测试反复重建容器时遗留过期清理线程。
   */
  private static final class DisposableLocalCacheStore extends LocalCacheStore implements DisposableBean {

    @Override
    public void destroy() {
      if (!(cache instanceof ConCurrentExpiringMap<?, ?> expiringMap)) {
        return;
      }
      try {
        cache.clear();
        Field executorField = ConCurrentExpiringMap.class.getDeclaredField("scheduledExecutor");
        executorField.setAccessible(true);
        Object executor = executorField.get(expiringMap);
        if (executor instanceof ScheduledExecutorService scheduledExecutor) {
          scheduledExecutor.shutdownNow();
        }
      } catch (ReflectiveOperationException e) {
        log.warn("failed to shutdown captcha local cache scheduler: {}", e.getMessage());
      }
    }
  }
}
