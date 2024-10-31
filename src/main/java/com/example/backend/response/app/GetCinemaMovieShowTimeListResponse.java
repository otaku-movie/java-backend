package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

@Data
class TheaterHallShowTime {
  Integer id;
  Integer theater_hall_id;
  String theater_hall_name;
  String start_time;
  String end_time;
}

@Data
class DateGroup {
  String date;
  List<TheaterHallShowTime> data;
}

@Data
public class GetCinemaMovieShowTimeListResponse {
  Integer cinema_id;
  String cinema_name;
  String cinema_address;
  String cinema_tel;
  List<DateGroup> data;

}
