package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("benefit_theater_stock")
public class BenefitTheaterStock {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("cinema_id")
  private Integer cinemaId;

  @TableField("benefit_id")
  private Integer benefitId;

  @TableField("quota")
  private Integer quota;

  @TableField("remaining")
  private Integer remaining;

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
