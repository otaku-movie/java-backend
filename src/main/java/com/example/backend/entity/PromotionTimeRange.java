package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("promotion_time_range")
public class PromotionTimeRange {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("promotion_id")
  private Integer promotionId;

  @TableField("name")
  private String name;

  @TableField("applicable_scope")
  private String applicableScope;

  @TableField("applicable_days")
  private String applicableDays;

  @TableField("start_time")
  private String startTime;

  @TableField("end_time")
  private String endTime;

  @TableField("price")
  private Integer price;

  @TableField("remark")
  private String remark;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
