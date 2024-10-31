package com.example.backend.query;

import lombok.Data;

@Data
public class CinemaListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;

  public CinemaListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
