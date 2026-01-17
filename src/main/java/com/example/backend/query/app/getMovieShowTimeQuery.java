package com.example.backend.query.app;

import com.example.backend.query.PaginationQuery;
import lombok.Data;

import java.util.List;

@Data
public class getMovieShowTimeQuery extends PaginationQuery {
  Integer movieId;
  List<Integer> specId;
  Integer subtitleId;
  // 特殊标签筛选
  Integer showTimeTagId;
  // 地区筛选
  Integer regionId;
  Integer prefectureId;
  Integer cityId;
  // 附近影院查询（单位：米）
  Double latitude;
  Double longitude;
  Double radius; // 搜索半径，单位：米，默认不限制
  // 版本筛选
  Integer versionCode;
  // 搜索关键词（影院名称或地址）
  String keyword;
  // 开场时间范围筛选
  String startTimeFrom; // 开场时间起始时间（格式：yyyy-MM-dd HH:mm，如 "2025-01-15 09:00" 或 30小时制 "2025-01-15 25:00"）
  String startTimeTo;   // 开场时间结束时间（格式：yyyy-MM-dd HH:mm，如 "2025-01-15 23:00" 或 30小时制 "2025-01-15 29:00"）
  Boolean use30HourFormat; // 是否使用30小时制（24:00-29:59 表示第二天的 00:00-05:59），默认 false（24小时制）
}
