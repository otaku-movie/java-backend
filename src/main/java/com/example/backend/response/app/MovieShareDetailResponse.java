package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

/**
 * 分享落地页 - 电影详情响应
 */
@Data
public class MovieShareDetailResponse {
  private Integer id;
  /** 主标题（中文/默认） */
  private String name;
  /** 原名（外语片名） */
  private String originalName;
  /** 海报 URL */
  private String cover;
  /** 简介 */
  private String description;
  /** 时长（分钟） */
  private Integer duration;
  /** 上映日期 */
  private String startDate;
  /** 1 未上映 / 2 上映中 / 3 上映结束 */
  private Integer status;
  /** 观看人数 */
  private Integer watchedCount;
  /** 想看人数 */
  private Integer wantToSeeCount;
  /** 类型/标签 */
  private List<String> tags;
  /** 平均评分（0~10） */
  private Double rating;
  /** 评分人数 */
  private Integer ratingCount;
  /** 官方主页 */
  private String homePage;
}
