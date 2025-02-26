package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShowTimeState {
  // 未开始
  no_started(1),
  // 上映中
  screening(2),
  // 已结束
  ended(3);

  private final int code;
}