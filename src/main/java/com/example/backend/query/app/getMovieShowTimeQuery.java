package com.example.backend.query.app;

import com.example.backend.query.PaginationQuery;
import lombok.Data;

import java.util.List;

@Data
public class getMovieShowTimeQuery extends PaginationQuery {
  Integer movieId;
  List<Integer> specId;
  Integer subtitleId;
  // 最后一级的地区id
  Integer regionId;
  Integer prefectureId;
  Integer cityId;
}
