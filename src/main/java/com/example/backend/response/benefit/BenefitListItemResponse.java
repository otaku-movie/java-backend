package com.example.backend.response.benefit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class BenefitListItemResponse {
  private Integer id;
  private Integer movieId;
  /** 关联重映计划（为空表示普通上映特典） */
  private Integer reReleaseId;
  private String movieName;
  /** 电影海报/封面 URL */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String movieCover;
  private String name;
  private Integer quantity;
  private String description;
  /** 特典阶段展示图（与详情一致，来自 benefit.image_urls JSON） */
  private List<String> imageUrls;
  private String startDate;
  /** 结束日期，无则不返回该字段 */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String endDate;
  private Integer orderNum;
  private Integer itemCount;
  /** 剩余数量（汇总该阶段下各影院库存；任一未知则为 null） */
  private Integer remaining;
  /** 阶段状态：字典 benefitPhaseStatus 1=之前 2=进行中 3=已结束 */
  private Integer status;
}
