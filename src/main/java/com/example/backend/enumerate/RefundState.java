package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundState {
  /** 无退款 */
  none(1),
  /** 退款中 */
  refunding(2),
  /** 已退款 */
  refunded(3),
  /** 退款失败 */
  refund_failed(4);

  private final int code;
}
