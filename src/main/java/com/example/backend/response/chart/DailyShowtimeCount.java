package com.example.backend.response.chart;

import lombok.Data;

/**
 * 单日场次总数（dashboard "未来 7 天场次" 柱图用）。
 *
 * <p>{@code date} 为 YYYY-MM-DD 字符串，与现有 {@code StatisticsOfDailyMovieScreenings.startTime}
 * 保持一致以便前端复用 echarts 配置。
 */
@Data
public class DailyShowtimeCount {
  String date;
  Long showtimeCount;
}
