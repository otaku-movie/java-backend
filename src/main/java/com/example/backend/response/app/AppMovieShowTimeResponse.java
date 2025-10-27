package com.example.backend.response.app;

import lombok.Data;
import java.util.List;

@Data
public class AppMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String cinemaTel;
  Integer totalShowTimes;  // 新增：总场次数
  List<ShowTimeInfo> showTimes;
}

