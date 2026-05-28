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
  List<DailyOrderStatistics> dailyOrderStatistics;
  List<DailyTransactionAmount> dailyTransactionAmount;
  /** 按注册/登录平台（Google / Apple / Email 等）聚合的用户数 */
  List<LoginPlatformStatistics> loginPlatformStatistics;
}
