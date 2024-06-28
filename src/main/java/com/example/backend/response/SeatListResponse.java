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
  Integer theater_hall_id;
  Integer x;
  Integer y;
  Integer z;
  Boolean selected;
  Boolean show;
  Boolean disabled;
  // 轮椅座
  Boolean wheel_chair;
  String seat_position_group;
  SeatAreaResponse area;
  Integer select_seat_state;
}
