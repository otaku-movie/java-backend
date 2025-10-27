package com.example.backend.query;

import lombok.Data;

@Data
public class PaginationQuery {
  Integer page;
  Integer pageSize;

  public PaginationQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
