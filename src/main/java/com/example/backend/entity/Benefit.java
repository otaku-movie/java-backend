package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("benefit")
public class Benefit {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("movie_id")
  private Integer movieId;

  @TableField("name")
  private String name;

  @TableField("start_date")
  private String startDate;

  @TableField("end_date")
  private String endDate;

  @TableField("order_num")
  private Integer orderNum;

  /** 阶段状态：字典 benefitPhaseStatus 1=未开始 2=进行中 3=已结束，null 时按起止日期计算 */
  @TableField("phase_status")
  private Integer phaseStatus;

  /** 特典数量（可选） */
  @TableField("quantity")
  private Integer quantity;

  /** 剩余数量，null=未知，0=已经没有了 */
  @TableField("remaining")
  private Integer remaining;

  /** 特典描述 */
  @TableField("description")
  private String description;

  /** 特典图片URL列表，JSON数组存储 */
  @TableField("image_urls")
  private String imageUrls;

  /** 放映类型限定：dict dimensionType，null=不限 */
  @TableField("dimension_type")
  private Integer dimensionType;

  /** 特效场次/规格限定（多选）：cinema_spec.id 列表，JSON数组存储，空=不限 */
  @TableField("spec_ids")
  private String specIds;

  /** 影院限定：0=不限，1=限定 */
  @TableField("cinema_limit_type")
  private Integer cinemaLimitType;

  /** 影院限定时的影院ID列表，JSON数组存储 */
  @TableField("cinema_ids")
  private String cinemaIds;

  @JsonIgnore
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonIgnore
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @JsonIgnore
  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
