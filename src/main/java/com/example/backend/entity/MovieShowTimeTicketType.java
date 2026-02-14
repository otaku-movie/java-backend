package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 场次限定票种规则：某场次下某票种的覆盖价与是否启用（仅默认规则时使用）
 */
@Data
@TableName("movie_show_time_ticket_type")
public class MovieShowTimeTicketType {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("show_time_id")
  private Integer showTimeId;

  @TableField("ticket_type_id")
  private Integer ticketTypeId;

  /** 本场次覆盖价格，null 表示用票种默认价 */
  @TableField("override_price")
  private BigDecimal overridePrice;

  /** 本场次是否启用该票种，默认 true */
  @TableField("enabled")
  private Boolean enabled;


  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
