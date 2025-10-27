package com.example.backend.query.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelOrderQuery {
  // 订单 ID
  @NotNull
  private Integer orderId;
}
