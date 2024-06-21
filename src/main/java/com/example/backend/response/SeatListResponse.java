package com.example.backend.response;

import com.example.backend.query.SeatAreaQuery;
import lombok.Data;

import java.util.List;

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
  Boolean show;
  Boolean disabled;
  // 轮椅座
  Boolean wheelChair;
  String seatPositionGroup;
  Integer seatType;
  SeatAreaResponse area;
}