package com.example.backend.query.benefit;

import com.example.backend.query.PaginationQuery;
import lombok.Data;

@Data
public class BenefitMovieListQuery extends PaginationQuery {
  /** 电影名称模糊搜索（可选） */
  private String movieName;
}

