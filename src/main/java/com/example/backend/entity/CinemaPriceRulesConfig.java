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

/** 影院促销配置：一影院一行，规则类型优先级、是否前售券等 */
@Data
@TableName("cinema_price_rules_config")
public class CinemaPriceRulesConfig {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("cinema_id")
  private Integer cinemaId;

  @TableField("remark")
  private String remark;

  @TableField("allow_muviticket")
  private Boolean allowMuviticket;

  @TableField("monthly_priority")
  private Integer monthlyPriority;
  @TableField("weekly_priority")
  private Integer weeklyPriority;
  @TableField("specific_date_priority")
  private Integer specificDatePriority;
  @TableField("time_range_priority")
  private Integer timeRangePriority;
  @TableField("fixed_price_priority")
  private Integer fixedPricePriority;
  @TableField("ticket_type_priority")
  private Integer ticketTypePriority;

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
