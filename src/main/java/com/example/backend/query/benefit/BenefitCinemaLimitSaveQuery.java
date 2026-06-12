package com.example.backend.query.benefit;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 仅更新某特典阶段的「影院限定」。
 * 用于库存分配页的「管理影院限定」快捷操作，避免走全量 saveBenefit 把其它字段覆盖掉。
 */
@Data
public class BenefitCinemaLimitSaveQuery {
  /** 特典阶段 ID */
  @NotNull
  private Integer benefitId;
  /** 限定影院 ID 列表；为空表示不限定（所有影院）。前端可传 cinemaLimitIds */
  @JsonAlias("cinemaLimitIds")
  private List<Integer> cinemaIds;
}
