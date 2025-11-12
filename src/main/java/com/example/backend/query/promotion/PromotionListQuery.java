package com.example.backend.query.promotion;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PromotionListQuery extends PaginationQuery {
  private Integer cinemaId;
  private String name;
}
