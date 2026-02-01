package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("refund")
public class Refund implements Serializable {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("order_number")
  private String orderNumber;

  @TableField("user_id")
  private Integer userId;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("reason")
  private String reason;

  /** 申请状态 1=无申请 2=已申请 3=处理中 4=已同意 5=已拒绝 */
  @TableField("apply_status")
  private Integer applyStatus;

  /** 退款状态 1=无退款 2=退款中 3=已退款 4=退款失败 */
  @TableField("refund_state")
  private Integer refundState;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("apply_time")
  private Date applyTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("process_time")
  private Date processTime;

  @TableField("processor_id")
  private Integer processorId;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;

  private static final long serialVersionUID = 1L;
}
