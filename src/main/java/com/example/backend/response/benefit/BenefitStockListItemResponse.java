package com.example.backend.response.benefit;

import lombok.Data;

@Data
public class BenefitStockListItemResponse {
  private Integer id;
  private Integer cinemaId;
  private String cinemaName;
  private Integer benefitId;
  private String benefitName;
  private Integer quota;
  private Integer remaining;
  /** 1=运营置为已领完 */
  private Integer manualSoldOut;
}
