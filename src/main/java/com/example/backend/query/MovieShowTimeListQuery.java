package com.example.backend.query;

import lombok.Data;

@Data
public class MovieShowTimeListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer movieId;
  private Integer cinemaId;
  private Integer theaterHallId;
  private String date;

  public MovieShowTimeListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
