package com.example.backend.response.chart;

import lombok.Data;

/**
 * 今日场次最多的 Top N 影院（dashboard 横向条形图）。
 *
 * <p>{@code brandName} 用于在条形 label 后展示品牌名，方便区分同名连锁影院。
 */
@Data
public class TopCinemaShowtime {
  Integer cinemaId;
  String cinemaName;
  String brandName;
  Long showtimeCount;
}
