package com.example.backend.query.benefit;

import lombok.Data;

/**
 * 标记用户反馈为已读：按特典 / 影院维度，benefitId 与 cinemaId 至少传一个。
 */
@Data
public class BenefitFeedbackReadQuery {
  private Integer benefitId;
  private Integer cinemaId;
}
