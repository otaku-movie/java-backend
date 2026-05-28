package com.example.backend.query.benefit;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BenefitFeedbackResetQuery {
  @NotNull
  private Integer benefitId;
  @NotNull
  private Integer cinemaId;
}
