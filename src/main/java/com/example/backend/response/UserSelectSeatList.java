package com.example.backend.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public
class UserSelectSeatList {
  private Integer x;
  private Integer y;
  private  Integer seatId;

  private String seatName;

  private BigDecimal areaPrice;

  private String areaName;

  private Integer movieTicketTypeId;

  private BigDecimal plusPrice;
}
