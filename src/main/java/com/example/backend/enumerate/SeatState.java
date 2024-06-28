package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum SeatState {
  // 可选择
  available(1),
  // 已锁定
  locked(2),
  // 已售出
  sold(3);

  private final int code;
}
