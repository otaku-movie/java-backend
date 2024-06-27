package com.example.backend.query.dict;

import lombok.Data;

@Data
public class DictListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;
  private String code;

  public DictListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}