package com.example.backend.response.benefit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class BenefitDetailResponse {
  private Integer id;
  private Integer movieId;
  /** 关联重映计划（为空表示普通上映特典） */
  private Integer reReleaseId;
  private String movieName;
  private String name;
  /** 特典数量 */
  private Integer quantity;
  /** 剩余数量，null=未知，0=已经没有了 */
  private Integer remaining;
  /** 特典描述 */
  private String description;
  /** 特典图片URL列表 */
  private List<String> imageUrls;
  /** 放映类型限定，null=不限 */
  private Integer dimensionType;
  /** 特效场次/规格限定（多选），空=不限 */
  private List<Integer> specIds;
  /** 规格名称列表（用于展示） */
  private List<String> specNames;
  /** 影院限定：0=不限，1=限定 */
  private Integer cinemaLimitType;
  /** 影院限定时的影院ID列表 */
  private List<Integer> cinemaIds;
  private String startDate;
  /** 结束日期，无则不返回该字段 */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String endDate;
  private Integer orderNum;
  /** 阶段状态：字典 benefitPhaseStatus 1=之前 2=进行中 3=已结束 */
  private Integer status;
}
