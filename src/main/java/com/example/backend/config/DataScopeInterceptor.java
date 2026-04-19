package com.example.backend.config;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.service.AdminDataScopeRlsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理后台 {@code /api/admin/**}：根据 users.data_scope 填充 RLS 线程上下文，供 {@link
 * RlsMybatisInterceptor} 写入 PostgreSQL 会话变量。
 */
@Slf4j
@Component
public class DataScopeInterceptor implements HandlerInterceptor {

  @Autowired private AdminDataScopeRlsService adminDataScopeRlsService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!StpUtil.isLogin()) {
      return true;
    }
    adminDataScopeRlsService.applyForLoggedInAdminUser();
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    RequestContextHolder.clearRls();
  }
}
