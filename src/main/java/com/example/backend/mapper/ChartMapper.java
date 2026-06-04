package com.example.backend.mapper;

import com.example.backend.response.chart.BrandShowtimeStatistics;
import com.example.backend.response.chart.ChartKpiTrends;
import com.example.backend.response.chart.DailyShowtimeCount;
import com.example.backend.response.chart.MovieDataQuality;
import com.example.backend.response.chart.PrefectureShowtimeStatistics;
import com.example.backend.response.chart.TopCinemaShowtime;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Dashboard 专用聚合查询。
 *
 * <p>所有方法均查询当前 {@code currentSchema}（crawl）下的爬虫镜像表，
 * 不涉及业务订单 / 用户。订单 / 用户类的统计依旧走 {@code MovieOrderMapper} / {@code UserMapper}。
 */
@Mapper
public interface ChartMapper {
  /** 影院品牌数（{@code brand.deleted = 0}）。 */
  Long countBrands();

  /** 影厅数（{@code theater_hall.deleted = 0}）。 */
  Long countTheaterHalls();

  /** 已匹配 TMDb 的电影数（{@code movie.tmdb_id IS NOT NULL}），分母用 {@code MovieMapper#selectCount}。 */
  Long countMoviesWithTmdb();

  /** 今日场次总数（{@code start_time::date = CURRENT_DATE}）。 */
  Long countTodayShowtimes();

  /** 未来 7 天每日场次（从 CURRENT_DATE 起 6 天，闭区间），空白日期返回 0 由前端处理。 */
  List<DailyShowtimeCount> next7DaysShowtimes();

  /** 今日按影院品牌的场次聚合，倒序。 */
  List<BrandShowtimeStatistics> todayBrandShowtimes();

  /** 今日场次最多的 Top 10 影院。 */
  List<TopCinemaShowtime> todayTopCinemas();

  /** 电影主数据各字段填充率原始计数（前端算百分比）。 */
  MovieDataQuality movieDataQuality();

  /**
   * 今日按都道府县聚合的场次（依赖 V12 后的 areas + cinema.prefecture_id）。
   * 没有 prefecture_id 的 cinema 整体被丢弃（这部分电影院尚未地址 fall-back 成功）。
   */
  List<PrefectureShowtimeStatistics> todayPrefectureShowtimes();

  /** 顶部 KPI 环比：今日场次 % vs 昨日、今日新增电影 / 影院数。 */
  ChartKpiTrends kpiTrends();
}
