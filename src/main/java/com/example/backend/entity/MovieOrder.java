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

  /**
   * 订单所属用户ID
   */
  @TableField(value = "user_id")
  private Integer userId;

  /**
   * 业务订单号（要求全局唯一）
   */
  @TableField(value = "order_number")
  private String orderNumber;

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
  private Date payTime;

  @TableField(value = "pay_method_id")
  private Integer payMethodId;

  /** 订单失败/取消/超时原因，如支付失败、座位锁定过期等 */
  @TableField(value = "failure_reason")
  private String failureReason;

  // 订单创建时间
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  // 支付截止时间（用于前端倒计时显示）
  @TableField(exist = false)
  private Date payDeadline;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}