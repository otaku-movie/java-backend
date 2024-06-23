package com.example.backend.controller;

import lombok.Data;

@Data
public class TheaterHallQuery {
  private Integer page;
  private Integer pageSize;
  private Integer cinemaId;
  private String name;
  private  Integer id;

  public TheaterHallQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
