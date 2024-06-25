package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayState {
  // 等待支付
  waiting_for_payment(1),
  // 支付中
  paying(2),
  // 支付成功
  payment_successful(3),
  // 支付失败
  payment_failed(4),
  // 取消支付
  canceled_payment(5);

  private final int code;
}
