package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisType {
  // 验证码
  verifyCode("verifyCode");

  private final String code;
}
