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
}
