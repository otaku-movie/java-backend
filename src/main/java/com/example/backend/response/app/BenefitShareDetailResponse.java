package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

/**
 * 分享落地页 - 入场者特典响应（公开可见字段）。
 */
@Data
public class BenefitShareDetailResponse {
  private Integer id;
  private Integer movieId;
  private String name;
  private String movieName;
  private String moviePoster;
  /** 特典物料展示图，可能多张。 */
  private List<String> imageUrls;
  private String description;
  private String startDate;
  private String endDate;
  /** 1 未开始 / 2 进行中 / 3 已结束，与 dict benefitPhaseStatus 一致。 */
  private Integer status;
}
