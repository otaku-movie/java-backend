package com.example.backend.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单超时消息
 * 用于 RabbitMQ 延迟消息处理订单超时
 */
@Data
public class OrderTimeoutMessage implements Serializable {
  private static final long serialVersionUID = 1L;
  
  /** 订单号 */
  private String orderNumber;
  
  /** 订单ID */
  private Integer orderId;
}
