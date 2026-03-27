package com.example.backend.query.benefit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BenefitSaveQuery {
  private Integer id;
  @NotNull(message = "{validator.benefit.movieId.required}")
  private Integer movieId;
  /** 可选：关联重映计划（为空表示普通上映特典） */
  private Integer reReleaseId;
  @NotBlank(message = "{validator.benefit.name.required}")
  private String name;
  /** 特典数量（可选） */
  private Integer quantity;
  /** 剩余数量，不填=未知，0=已经没有了 */
  private Integer remaining;
  /** 特典描述 */
  private String description;
  /** 特典图片URL列表 */
  private List<String> imageUrls;
  /** 放映类型限定，null=不限。前端可传 limitDimensionTypes 数组，取首个 */
  private Integer dimensionType;
  /** 特效场次/规格限定（多选），空=不限。前端可传 limitSpecIds */
  @JsonAlias("limitSpecIds")
  private List<Integer> specIds;
  /** 影院限定：0=不限，1=限定。有 cinemaLimitIds 时自动为 1 */
  private Integer cinemaLimitType;
  /** 影院限定时的影院ID列表。前端可传 cinemaLimitIds */
  @JsonAlias("cinemaLimitIds")
  private List<Integer> cinemaIds;

  /** 前端传 limitDimensionTypes 数组时，取首个作为 dimensionType */
  @JsonSetter("limitDimensionTypes")
  public void setLimitDimensionTypes(List<Integer> list) {
    if (list != null && !list.isEmpty()) {
      this.dimensionType = list.get(0);
    }
  }
  @NotBlank(message = "{validator.benefit.startDate.required}")
  private String startDate;
  /** 可选，不传或空表示无结束日期 */
  private String endDate;
  private Integer orderNum;
}
