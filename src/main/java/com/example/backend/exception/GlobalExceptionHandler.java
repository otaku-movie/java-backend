package com.example.backend.exception;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.util.SaResult;
import com.example.backend.entity.RestBean;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 全局异常拦截（拦截项目中的NotLoginException异常）
  @ExceptionHandler(NotLoginException.class)
  public RestBean handlerNotLoginException(
    NotLoginException nle,
    HttpServletResponse response
  ) throws IOException {
    // 打印堆栈，以供调试
    nle.printStackTrace();

    // 判断场景值，定制化异常信息
    String message;
    switch (nle.getType()) {
      case NotLoginException.NOT_TOKEN:
        message = "未能读取到有效 token";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
        break;
      case NotLoginException.INVALID_TOKEN:
        message = "token 无效";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
        break;
      case NotLoginException.TOKEN_TIMEOUT:
        message = "token 已过期";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
        break;
      case NotLoginException.NO_PREFIX:
        message = "未按照指定前缀提交 token";
        break;
      default:
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录，请去登录");
        message = "当前会话未登录";
        break;
    }

    // 返回给前端
    return RestBean.error(401, message);
  }
}
