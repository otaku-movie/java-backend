package com.example.backend.response.cinema;

import com.example.backend.annotation.Interceptor;
import com.example.backend.response.MovieShowTimeList;
import lombok.Data;

import java.util.List;


@Data
public class CinemaScreeningResponse {
  Integer id;
  String name;
  String date;
  List<MovieShowTimeList> children;
}
