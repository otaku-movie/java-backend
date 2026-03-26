package com.example.backend.query.benefit;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BenefitFeedbackListQuery extends PaginationQuery {
  private Integer cinemaId;
  private Integer benefitId;
  private Integer feedbackType;
}
