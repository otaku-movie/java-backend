package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum SeatState {
  // 可选择
  available(1),
  // 已锁定（没订单号）
  locked_not_orderId(2),
  // 已锁定（有订单号，未付款）
  locked_not_paid(3),
  // 已售出
  sold(4);

  private final int code;
}
