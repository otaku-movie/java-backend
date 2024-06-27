package com.example.backend.response.chart;

import lombok.Data;

import java.util.List;

@Data
public class ChartResponse {
  Long userCount;
  Long movieCount;
  Long cinemaCount;
  Long showTimeCount;
  List<StatisticsUserCount> statisticsUserData;
  List<StatisticsOfDailyMovieScreenings> statisticsOfDailyMovieScreenings;
}
