package com.example.backend.response.chart;

import lombok.Data;

import java.util.List;

@Data
class Movie {
  Integer movie_id;
  String movie_name;
  Long movie_count;
}

@Data
public class StatisticsOfDailyMovieScreenings {
  String start_time;
  Long total_count;
  List<Movie> movie;
}
