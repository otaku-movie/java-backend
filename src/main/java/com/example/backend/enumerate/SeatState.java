package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum SeatState {
  // 未选择
  available(1),
  // 已选择 未锁定（没创建订单），
  selected(2),
  // 已锁定 (已创建订单，未支付)
  locked(3),
  // 已售出
  sold(4);

  private final int code;
}
