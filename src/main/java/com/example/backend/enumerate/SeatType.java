package com.example.backend.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@AllArgsConstructor
public enum SeatType {
  ROW("row"),
  COLUMN("column");

  @EnumValue
  private final String type;
}