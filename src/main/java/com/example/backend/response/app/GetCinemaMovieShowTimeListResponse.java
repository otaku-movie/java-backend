package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

@Data
class TheaterHallShowTime {
  Integer id;
  Integer theaterHallId;
  String theaterHallName;
  String startTime;
  String endTime;
}

@Data
class DateGroup {
  String date;
  List<TheaterHallShowTime> data;
}

@Data
public class GetCinemaMovieShowTimeListResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  String cinemaTel;
  List<DateGroup> data;

}
