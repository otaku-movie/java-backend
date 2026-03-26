package com.example.backend.query.benefit;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BenefitStockListQuery extends PaginationQuery {
  /** 按阶段ID筛选 */
  private Integer benefitId;
  /** 按影院ID筛选 */
  private Integer cinemaId;
}
