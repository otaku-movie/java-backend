package com.example.backend.query;

import lombok.Data;

@Data
public class GetCinemaMovieShowTimeListQuery {
  Integer cinemaId;
  Integer movieId;
  /**
   * 版本代码（字典：原版、中文配音、日语配音等）
   */
  Integer versionCode;
  // 开场时间范围筛选
  String startTimeFrom; // 开场时间起始时间（格式：yyyy-MM-dd HH:mm，如 "2025-01-15 09:00" 或 30小时制 "2025-01-15 25:00"）
  String startTimeTo;   // 开场时间结束时间（格式：yyyy-MM-dd HH:mm，如 "2025-01-15 23:00" 或 30小时制 "2025-01-15 29:00"）
  Boolean use30HourFormat; // 是否使用30小时制（24:00-29:59 表示第二天的 00:00-05:59），默认 false（24小时制）
}
