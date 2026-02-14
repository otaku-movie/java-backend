package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatGroup {
  @NotNull
  // x 坐标
  Integer x;
  @NotNull
  // y 坐标
  Integer y;
  @NotNull
  Integer seatId;
  /** 电影票类型；固定票价(pricing_mode=2)时可为 null，后端使用 fixed_amount */
  Integer movieTicketTypeId;
}
