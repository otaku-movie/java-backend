package com.example.backend.response.chart;

import lombok.Data;

/**
 * 今日各影院品牌的场次聚合（dashboard 品牌占比饼图）。
 *
 * <p>口径：{@code movie_show_time.start_time::date = CURRENT_DATE} && deleted=0。
 * 品牌过滤掉 deleted=1 的，name 直接来自 {@code brand.name}。
 */
@Data
public class BrandShowtimeStatistics {
  Integer brandId;
  String brandName;
  Long showtimeCount;
}
