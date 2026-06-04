package com.example.backend.response.chart;

import lombok.Data;

import java.util.List;

@Data
public class ChartResponse {
  Long userCount;
  Long movieCount;
  Long cinemaCount;
  /** 全量场次数（历史 + 今后），与原字段含义保持不变。 */
  Long showTimeCount;
  /** 影院品牌数（爬虫维度，dashboard 顶部 KPI 用）。 */
  Long brandCount;
  /** 影厅数（爬虫维度，dashboard 顶部 KPI 用）。 */
  Long theaterHallCount;
  /** TMDb 已匹配的电影数，分母 = {@link #movieCount}。 */
  Long tmdbMatchedMovieCount;
  /** 今日场次数（{@code start_time::date = CURRENT_DATE}）。 */
  Long todayShowTimeCount;
  List<StatisticsUserCount> statisticsUserData;
  List<StatisticsOfDailyMovieScreenings> statisticsOfDailyMovieScreenings;
  List<DailyOrderStatistics> dailyOrderStatistics;
  List<DailyTransactionAmount> dailyTransactionAmount;
  /** 按注册/登录平台（Google / Apple / Email 等）聚合的用户数 */
  List<LoginPlatformStatistics> loginPlatformStatistics;
  /** 今日各影院品牌场次聚合，饼图。 */
  List<BrandShowtimeStatistics> todayBrandShowtimes;
  /** 今日场次 Top 10 影院，横向条形图。 */
  List<TopCinemaShowtime> todayTopCinemas;
  /** 未来 7 天每日场次，柱图。 */
  List<DailyShowtimeCount> next7DaysShowtimes;
  /** 今日按都道府县场次，横向条形图（依赖 V12 后的 areas + cinema.prefecture_id）。 */
  List<PrefectureShowtimeStatistics> todayPrefectureShowtimes;
  /** 电影主数据填充率原始计数，前端算百分比。 */
  MovieDataQuality movieDataQuality;
  /** 顶部 KPI 环比（相对昨日）。 */
  ChartKpiTrends kpiTrends;
}
