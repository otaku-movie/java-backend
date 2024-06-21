package com.example.backend.query;

import lombok.Data;

@Data
public class SeatQuery {
  Integer id;
  Integer theaterHallId;
  Integer x;
  Integer y;
  Integer z;
//  Integer xAxis;
//  Integer yAxis;
//  Integer zAxis;
  Boolean show;
  Boolean disabled;
  // 轮椅座
  Boolean wheelChair;
  String seatPositionGroup;
  Integer seatType;
}
