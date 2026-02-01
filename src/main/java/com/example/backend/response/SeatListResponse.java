package com.example.backend.response;

import lombok.Data;

@Data
class SeatAreaResponse {
  Integer id;
  String name;
  Integer price;
  String color;
//  List<String> seat;
}

@Data
public class SeatListResponse {
  Integer id;
  Integer theaterHallId;
  Integer x;
  Integer y;
  Integer z;
  String rowName;
  String seatName;
  Boolean selected;
  Boolean show;
  Boolean disabled;
  // 轮椅座
  Boolean wheelChair;
  String seatPositionGroup;
  // 区域价格（从 seat_area.price 展平，便于其他模块直接使用）
  Integer areaPrice;
  // 区域ID（从 seat.seat_area_id 获取，便于直接查询区域信息）
  Integer seatAreaId;
  SeatAreaResponse area;
  Integer selectSeatState;
}
