package com.example.backend.response.app;

import lombok.Data;

@Data
public class AppBeforeMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String cinemaTel;
  Double cinemaLatitude;  // 影院纬度
  Double cinemaLongitude;  // 影院经度
  Integer id;
  Integer theaterHallId;
  String theaterHallName;
  String startTime;
  String endTime;
  String specName;
  Integer totalSeats;  // 新增：总座位数
  Integer selectedSeats;  // 新增：已选座位数
  Double distance;  // 距离（米），仅在附近查询时返回
  Integer movieVersionId;
  Integer versionCode;
}
