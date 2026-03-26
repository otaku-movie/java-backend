package com.example.backend.query.benefit;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** App 端提交特典反馈（如：该影院该阶段已领完） */
@Data
public class BenefitFeedbackSubmitQuery {
  @NotNull(message = "{validator.benefitFeedback.cinemaId.required}")
  private Integer cinemaId;
  @NotNull(message = "{validator.benefitFeedback.benefitId.required}")
  private Integer benefitId;
  /** 1=已领完，默认 1 */
  private Integer feedbackType = 1;
}
