package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

@Data
public class AppRootMovieShowTimeResponse {
  String date;
  List<AppMovieShowTimeResponse> data;
}
