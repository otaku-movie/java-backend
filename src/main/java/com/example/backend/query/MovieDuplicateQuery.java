package com.example.backend.query;

import lombok.Data;

/**
 * 重复电影候选组查询（后台「电影去重合并」页 - 自动分组模式）。
 */
@Data
public class MovieDuplicateQuery {
  private Integer page;
  private Integer pageSize;

  public MovieDuplicateQuery() {
    this.page = 1;
    this.pageSize = 10;
  }
}
