package com.example.backend.response.app;

import lombok.Data;

@Data
public class AppBeforeMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String startTime;
  String endTime;
}
