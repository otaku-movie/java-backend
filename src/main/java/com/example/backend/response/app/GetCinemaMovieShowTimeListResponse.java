package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

@Data
public class GetCinemaMovieShowTimeListResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaFullAddress;
  String cinemaTel;
  List<DateGroup> data;

}
