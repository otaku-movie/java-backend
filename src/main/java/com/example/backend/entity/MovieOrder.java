package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@TableName(value ="movie_order")
@Data
public class MovieOrder implements Serializable {
  /**
   *
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   *
   */
  @TableField(value = "movie_show_time_id")
  private Integer movieShowTimeId;

  @TableField(value = "movie_id")
  private Integer movieId;

  // 订单总价
  @TableField(value = "order_total")
  private BigDecimal orderTotal;

  // 订单状态
  @TableField(value = "order_state")
  private Integer orderState;

  @TableField(value = "pay_total")
  private BigDecimal payTotal;

  @TableField(value = "pay_state")
  private Integer payState;

  @TableField(value = "pay_time")
  private Integer payTime;

  @TableField(value = "pay_method")
  private Integer orderMethod;

  // 订单创建时间
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}