package com.example.backend.annotation;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.UserRole;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.response.RolePermissionButton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class Interceptor implements HandlerInterceptor {
  @Autowired
  UserRoleMapper userRoleMapper;

  @Autowired
  RoleMapper roleMapper;

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws AccessDeniedException {
    // 获取注解
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      Method method = handlerMethod.getMethod();

      if (method.isAnnotationPresent(CheckPermission.class)) {
        CheckPermission checkPermission = method.getAnnotation(CheckPermission.class);
        String code = checkPermission.code();
        Integer userId = StpUtil.getLoginIdAsInt();
        // 获取用户角色
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        if (userRoles != null) {
          UserRole userRole = userRoles.get(0);
          // 获取角色的按钮权限
          List<RolePermissionButton> rolePermissionButton = roleMapper.rolePermissionButton(userRole.getRoleId());

          System.out.println(rolePermissionButton);
          HashMap<String, RolePermissionButton> map = new HashMap<>();
          rolePermissionButton.forEach(item -> {
            if (map.get(item.getApi_code())  == null && item.getApi_code() != null) {
              map.put(item.getApi_code(), item);
            }
          });

          if (map.get(code) != null) {
            System.out.println("yes");
            return true;
          } else {
            System.out.println("no");
            response.setStatus(403);
            throw new AccessDeniedException("无权限");
          }
        }
      }
    }
    return true;
  }
}
