package com.example.backend.response.app;

import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
public class AppMovieShowTimeDetail {
  Integer id;
  String date;
  String startTime;
  String endTime;
  Integer status;
  Integer movieId;
  String movieName;
  Integer cinemaId;
  String cinemaName;
  Integer theaterHallId;
  String theaterHallName;

}
