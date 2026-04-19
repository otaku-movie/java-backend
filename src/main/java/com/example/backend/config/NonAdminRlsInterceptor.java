package com.example.backend.config;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.enumerate.DataScope;
import com.example.backend.service.AdminDataScopeRlsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 非 {@code /api/admin/**} 的 {@code /api/**} 请求默认写入「平台级」RLS，使 App/公共接口在启用 RLS 后仍能访问全量数据。
 *
 * <p>管理后台 SPA 会带 {@code role-id} 请求头且使用同一套 Sa-Token 登录；此类请求对共用路径（如 {@code
 * /api/cinema/list}）须按当前用户数据范围写入 RLS，否则影院/场次等列表会误显示全量。
 */
@Component
public class NonAdminRlsInterceptor implements HandlerInterceptor {

  @Autowired private AdminDataScopeRlsService adminDataScopeRlsService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String uri = request.getRequestURI();
    if (uri == null || !uri.contains("/api/")) {
      return true;
    }
    if (uri.contains("/api/admin/")) {
      return true;
    }
    String roleId = request.getHeader("role-id");
    if (roleId != null && !roleId.isBlank() && StpUtil.isLogin()) {
      adminDataScopeRlsService.applyForLoggedInAdminUser();
    } else {
      RequestContextHolder.setRls("level", DataScope.PLATFORM.getCode());
      RequestContextHolder.setRls("org_id", "");
      RequestContextHolder.setRls("cinema_ids", "");
      RequestContextHolder.setRls("user_id", "");
    }
    return true;
  }
}
