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
}
