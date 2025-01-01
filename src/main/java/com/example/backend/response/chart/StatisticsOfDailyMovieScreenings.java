package com.example.backend.response.chart;

import lombok.Data;

import java.util.List;

@Data
class Movie {
  Integer movieId;
  String movieName;
  Long movieCount;
}

@Data
public class StatisticsOfDailyMovieScreenings {
  String startTime;
  Long totalCount;
  List<Movie> movie;
}
