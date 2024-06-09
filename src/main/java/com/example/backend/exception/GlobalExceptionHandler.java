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
  // 自定义校验错误的返回信息
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
}
