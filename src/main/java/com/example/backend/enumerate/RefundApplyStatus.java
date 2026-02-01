package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundApplyStatus {
  /** 无申请 */
  none(1),
  /** 已申请 */
  applied(2),
  /** 处理中 */
  processing(3),
  /** 已同意 */
  approved(4),
  /** 已拒绝 */
  rejected(5);

  private final int code;
}
