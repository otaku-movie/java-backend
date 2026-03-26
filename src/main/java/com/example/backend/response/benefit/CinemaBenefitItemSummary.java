package com.example.backend.response.benefit;

import lombok.Data;

/** 影院特典汇总中的单条：阶段、配额、剩余 */
@Data
public class CinemaBenefitItemSummary {
  private Integer benefitId;
  private String benefitName;
  private Integer quota;
  private Integer remaining;
}
