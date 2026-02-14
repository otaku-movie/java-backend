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
@TableName("promotion_monthly_day")
public class PromotionMonthlyDay {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("cinema_id")
  private Integer cinemaId;

  @TableField("name")
  private String name;

  @TableField("day_of_month")
  private Integer dayOfMonth;

  @TableField("price")
  private Integer price;

  /** 规则优先级，数值越小越优先，同类型内排序 */
  @TableField("priority")
  private Integer priority;

  /** 是否启用 */
  @TableField("enabled")
  private Boolean enabled;

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
