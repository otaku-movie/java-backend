package com.example.backend.response.app;

import lombok.Data;

@Data
public class AppBeforeMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String cinemaTel;
  Integer id;
  Integer theaterHallId;
  String theaterHallName;
  String startTime;
  String endTime;
  String specName;
  Integer totalSeats;  // 新增：总座位数
  Integer selectedSeats;  // 新增：已选座位数
}
