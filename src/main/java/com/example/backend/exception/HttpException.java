package com.example.backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.utils.MessageUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.jdbc.Null;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class HttpException {
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<RestBean<Null>> handleAccessDeniedException(AccessDeniedException ex) {
//    HttpStatus status = HttpStatus.FORBIDDEN;
//    String message = MessageUtils.getMessage("error.notPermission");
//    return RestBean.error(ResponseCode.NOT_PERMISSION.getCode(), message);
    RestBean<Null> result = RestBean.error(HttpStatus.FORBIDDEN.value(), MessageUtils.getMessage("error.notPermission"));
    return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
  }


//  @ExceptionHandler(Exception.class)
//  public ResponseEntity<RestBean<Null>> handleException(Exception ex) {
//    ex.printStackTrace();  // 打印异常栈信息
//    RestBean<Null> result = RestBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), MessageUtils.getMessage("error.systemError"));
//    return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
//  }

  // 全局登录异常拦截（拦截项目中的NotLoginException异常）
  @ExceptionHandler(NotLoginException.class)
  public ResponseEntity<RestBean<Null>> handlerNotLoginException(
    NotLoginException nle,
    HttpServletResponse response
  ) throws IOException {
    // 打印堆栈，以供调试
    nle.printStackTrace();

    // 判断场景值，定制化异常信息
    // String message;
    // switch (nle.getType()) {
    //   case NotLoginException.NOT_TOKEN:
    //     message = "未能读取到有效 token";
    //     response.setStatus(HttpStatus.UNAUTHORIZED.value());
    //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
    //     break;
    //   case NotLoginException.INVALID_TOKEN:
    //     message = "token 无效";
    //     response.setStatus(HttpStatus.UNAUTHORIZED.value());
    //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
    //     break;
    //   case NotLoginException.TOKEN_TIMEOUT:
    //     message = "token 已过期";
    //     response.setStatus(HttpStatus.UNAUTHORIZED.value());
    //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录过期请重新登录");
    //     break;
    //   case NotLoginException.NO_PREFIX:
    //     message = "未按照指定前缀提交 token";
    //     break;
    //   default:
    //     response.setStatus(HttpStatus.UNAUTHORIZED.value());
    //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录，请去登录");
    //     message = "当前会话未登录";
    //     break;
    // }

    RestBean<Null> result = RestBean.error(HttpStatus.UNAUTHORIZED.value(), MessageUtils.getMessage("error.loginExpired"));
    return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
  }


}
