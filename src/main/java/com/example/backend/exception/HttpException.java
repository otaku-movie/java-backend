package com.example.backend.exception;

import com.example.backend.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class HttpException {
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public RestBean handleAccessDeniedException(AccessDeniedException ex) {
    return RestBean.error(403, "没权限");
  }
}
