package com.example.backend.response.app;

import lombok.Data;
import java.util.List;

@Data
public class AppMovieShowTimeResponse {
  Integer cinema_id;
  String cinema_name;
  String cinema_address;
  List<Time> time;
}

