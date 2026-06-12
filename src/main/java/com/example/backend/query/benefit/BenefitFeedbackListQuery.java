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
  /** 已读过滤：0=只看未读 1=只看已读 null=全部 */
  private Integer isRead;
}
