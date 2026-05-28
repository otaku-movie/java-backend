package com.example.backend.response.app;

import lombok.Data;

/**
 * C 端官网首页 - 精选海报项
 */
@Data
public class HomeHighlightItem {
  private Integer id;
  /** 主标题 */
  private String name;
  /** 原名 */
  private String originalName;
  /** 海报 URL */
  private String cover;
  /** 上映年份（从 startDate 截取） */
  private String year;
  /** 片长（分钟） */
  private Integer duration;
  /** 平均评分（0~10，无评分时为 null） */
  private Double rating;
}
