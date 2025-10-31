package com.example.backend.response.app;

import lombok.Data;
import java.util.List;

@Data
public class AppMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String cinemaTel;
  Double cinemaLatitude;  // 影院纬度
  Double cinemaLongitude;  // 影院经度
  Integer totalShowTimes;  // 新增：总场次数
  Double distance;  // 距离（米），仅在附近查询时返回
  List<ShowTimeInfo> showTimes;
}

