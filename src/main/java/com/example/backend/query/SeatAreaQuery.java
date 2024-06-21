package com.example.backend.query;

import lombok.Data;

import java.util.List;

@Data
public class SeatAreaQuery {
  Integer id;
  String name;
  Integer price;
  String color;
  List<String> seat;
}
