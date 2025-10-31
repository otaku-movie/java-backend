package com.example.backend.query;

import lombok.Data;

@Data
public class CinemaListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;
  // 地区筛选
  private Integer regionId;
  private Integer prefectureId;
  private Integer cityId;
  // 品牌筛选
  private Integer brandId;
  // 规格筛选
  private Integer specId;
  // 附近影院查询（单位：米）
  private Double latitude;
  private Double longitude;
  private Double radius; // 搜索半径，单位：米，默认不限制

  public CinemaListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
