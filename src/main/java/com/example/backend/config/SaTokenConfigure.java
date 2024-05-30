package com.example.backend.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
  // 注册拦截器
  // 注册 Sa-Token 拦截器，打开注解式鉴权功能
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
  }
//  @Override
//  public void addInterceptors(InterceptorRegistry registry) {
//    // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
//    registry
//      .addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
////      .addPathPatterns("/**")
//      .excludePathPatterns("/user/login");
//  }
}
