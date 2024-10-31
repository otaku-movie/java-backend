package com.example.backend.response.app;

import lombok.Data;

@Data
public class AppBeforeMovieShowTimeResponse {
  Integer cinema_id;
  String cinema_name;
  String cinema_address;
  String start_time;
  String end_time;
}
