package com.example.backend.response.chart;

import lombok.Data;

/**
 * 电影主数据填充率（dashboard "数据质量" 进度条）。
 *
 * <p>分子分母都使用 {@code movie} 表 deleted=0 的行，
 * 比率 = 各 with{X} / {@link #totalMovies}。前端单独算百分比。
 */
@Data
public class MovieDataQuality {
  Long totalMovies;
  Long withTmdb;
  Long withDescription;
  Long withReleaseDate;
  Long withHomePage;
  Long withMovieRate;
  Long withLevel;
}
