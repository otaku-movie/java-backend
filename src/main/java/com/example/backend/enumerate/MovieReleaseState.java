package com.example.backend.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovieReleaseState {
  notReleased(1),
  nowShowing(2),
  ended(3);

  @EnumValue
  private final Integer type;
}
