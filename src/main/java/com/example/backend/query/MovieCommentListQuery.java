package com.example.backend.query;

import lombok.Data;

@Data
public class MovieCommentListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  // 是否平铺
  private Boolean flattern;

  public MovieCommentListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
    this.flattern = true;
  }
}
