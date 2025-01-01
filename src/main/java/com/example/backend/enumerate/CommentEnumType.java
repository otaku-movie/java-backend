package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentEnumType {
  // 订单已创建
  comment("comment"),
  // 订单完成
  reply("reply");

  private final String code;
}
