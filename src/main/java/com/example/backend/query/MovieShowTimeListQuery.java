package com.example.backend.query;

import lombok.Data;

@Data
public class MovieShowTimeListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public MovieShowTimeListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
