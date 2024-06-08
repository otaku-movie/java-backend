package com.example.backend.exception;
import cn.dev33.satoken.exception.NotLoginException;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.utils.MessageUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.ReflectionException;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RestBean handleValidationExceptions(MethodArgumentNotValidException ex) {
    // 最初のエラーを取得
    FieldError firstError = (FieldError) ex.getBindingResult().getAllErrors().get(0);

    return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), firstError.getDefaultMessage());
  }
  @ExceptionHandler(value = ReflectionException.class)
  public RestBean exception(ReflectionException e) {
    System.out.println(e);
    return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.systemError"));
  }

  // 全局登录异常拦截（拦截项目中的NotLoginException异常）
  @ExceptionHandler(NotLoginException.class)
  public RestBean handlerNotLoginException(
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

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, MessageUtils.getMessage("error.loginExpired"));
    // 返回给前端
    return RestBean.error(ResponseCode.LOGIN_EXPIRED.getCode(), MessageUtils.getMessage("error.loginExpired"));
  }
}
