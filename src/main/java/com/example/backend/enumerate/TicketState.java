package com.example.backend.enumerate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketState {
  // 待生成
  to_be_generated(1),
  // 生成成功
  generation_successful(2),
  // 生成失败
  generation_failed(3),
  // 已使用
  ticket_used(4),
  // 已退票
  ticket_refunded(5);

  private final int code;
}