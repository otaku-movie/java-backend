package com.example.backend.query.order;

import lombok.Data;

import java.util.List;

@Data
public class MovieOrderListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private Integer movieId;
  private Integer cinemaId;
  private Integer theaterHallId;
  private Integer orderState;
  private Integer payState;
  private String orderStartTime;
  private String orderEndTime;

  public MovieOrderListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}