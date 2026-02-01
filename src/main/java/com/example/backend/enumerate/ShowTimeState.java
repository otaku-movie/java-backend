package com.example.backend.enumerate;

public enum ShowTimeState {
  // 未开始
  no_started(1),
  // 上映中
  screening(2),
  // 已结束
  ended(3);

  private final int code;

  ShowTimeState(int code) { this.code = code; }

  public int getCode() { return code; }
}