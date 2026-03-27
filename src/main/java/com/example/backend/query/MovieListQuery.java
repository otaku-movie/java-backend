package com.example.backend.query;

import lombok.Data;

@Data
public class MovieListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  /** 用于重映列表等按 movieId 过滤的场景（兼容老的 id 字段） */
  private Integer movieId;
  private Integer status;
  private String name;
  /**
   * 是否筛选“存在重映计划”的电影：
   * - null：不筛选
   * - 1：仅重映过
   * - 0：仅未重映
   */
  private Integer hasReRelease;

  public MovieListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}