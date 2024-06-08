package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
  SUCCESS(1),
  ERROR(0),
  NOT_PERMISSION(403),
  // 参数错误
  PARAMETER_ERROR(-1),
  LOGIN_EXPIRED(401),
  // 数据已存在
  REPEAT(-2);

  private final int code;
}