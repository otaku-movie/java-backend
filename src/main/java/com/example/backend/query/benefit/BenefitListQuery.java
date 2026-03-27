package com.example.backend.query.benefit;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BenefitListQuery extends PaginationQuery {
  /** 按电影ID筛选 */
  private Integer movieId;
  /** 可选：按重映计划筛选（为空表示普通上映特典） */
  private Integer reReleaseId;
  /** 阶段名称模糊 */
  private String name;
}
