package com.example.backend.response.app;

import lombok.Data;
import java.util.List;

@Data
public class AppMovieShowTimeResponse {
  Integer cinemaId;
  String cinemaName;
  String cinemaAddress;
  List<Time> time;
}

