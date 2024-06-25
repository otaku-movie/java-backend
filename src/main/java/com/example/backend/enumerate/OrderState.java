package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderState {
  // 订单已创建
  order_created(1),
  // 订单完成
  order_succeed(2),
  // 订单失败
  order_failed(3),
  // 取消订单
  canceled_order(4),
  // 订单超时
  order_timeout(5);

  private final int code;
}
