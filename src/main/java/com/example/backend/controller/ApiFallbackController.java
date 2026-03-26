package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兜底：未匹配的 /api/** 请求由此处理，返回 404 JSON，
 * 避免落入 ResourceHttpRequestHandler 导致 500 或方法不允许。
 */
@RestController
public class ApiFallbackController {

  @RequestMapping(value = "/api/**", method = { RequestMethod.GET, RequestMethod.POST,
      RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public RestBean<?> apiNotFound() {
    return RestBean.error(ResponseCode.RESOURCE_NOT_FOUND.getCode(), "接口不存在");
  }
}
