package com.example.backend.service;

import com.example.backend.entity.MovieShowTime;
import com.example.backend.query.MovieShowTimeQuery;

import java.text.ParseException;
import java.util.List;

public interface MovieShowTimeService {
  List<MovieShowTime> getSortedMovieShowTimes(MovieShowTimeQuery query, String format);
  void saveMovieShowTimeIfNotExists(MovieShowTimeQuery query, String format) throws ParseException;
  boolean check(List<MovieShowTime> list, String format, MovieShowTimeQuery query);

  /** 定时任务：根据 start_time/end_time 更新场次放映状态（未上映、上映中、已结束） */
  void updateScreeningState();

  /** 定时任务：根据 publish_at、sale_open_at 更新场次公开状态与可购票状态 */
  void updatePublishAndCanSaleState();
}
