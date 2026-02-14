package com.example.backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.utils.MessageUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.ReflectionException;
import java.io.IOException;

/**
 * 全局异常处理器
 * 统一处理所有异常，支持国际化和状态码
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  
  /**
   * 处理业务异常
   */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.OK)
  @SuppressWarnings("unchecked")
  public RestBean<Object> handleBusinessException(BusinessException ex) {
    String message = MessageUtils.getMessage(ex.getMessageKey(), ex.getMessageArgs());
    RestBean<Object> restBean = RestBean.error(ex.getResponseCode().getCode(), message);
    // 如果有额外数据（如不可用座位列表），添加到响应中
    if (ex.getExtraData() != null && !ex.getExtraData().isEmpty()) {
      restBean.setData(ex.getExtraData());
    }
    return restBean;
  }

  /**
   * 处理参数验证异常
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RestBean handleValidationExceptions(MethodArgumentNotValidException ex) {
    FieldError firstError = (FieldError) ex.getBindingResult().getAllErrors().get(0);
    // 使用原有的 PARAMETER_ERROR 状态码
    return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), firstError.getDefaultMessage());
  }

  /**
   * 处理未登录异常
   */
  @ExceptionHandler(NotLoginException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public RestBean handleNotLoginException(NotLoginException ex) {
    String message = MessageUtils.getMessage(MessageKeys.Error.LOGIN_EXPIRED);
    return RestBean.error(ResponseCode.LOGIN_EXPIRED.getCode(), message);
  }

  /**
   * 处理反射异常
   */
  @ExceptionHandler(value = ReflectionException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public RestBean handleReflectionException(ReflectionException e) {
    log.error("反射异常: {}", e.getMessage(), e);
    String message = MessageUtils.getMessage(MessageKeys.Error.SYSTEM);
    return RestBean.error(ResponseCode.ERROR.getCode(), message);
  }

  /**
   * 处理非法参数异常（业务校验不通过）
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public RestBean handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("参数校验失败: {}", ex.getMessage());
    return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), ex.getMessage());
  }

  /**
   * 处理运行时异常
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public RestBean handleRuntimeException(RuntimeException ex) {
    log.error("运行时异常: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
    String message = MessageUtils.getMessage(MessageKeys.Error.SYSTEM);
    return RestBean.error(ResponseCode.ERROR.getCode(), message);
  }

  /**
   * 处理通用异常
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public RestBean handleException(Exception ex) {
    log.error("未捕获异常: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
    String message = MessageUtils.getMessage(MessageKeys.Error.SYSTEM);
    return RestBean.error(ResponseCode.ERROR.getCode(), message);
  }
}
