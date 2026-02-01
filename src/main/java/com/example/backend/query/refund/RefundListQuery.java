package com.example.backend.query.refund;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RefundListQuery {
  @Min(value = 1, message = "页码不能小于1")
  private Integer page = 1;
  @Min(value = 1, message = "每页条数不能小于1")
  @Max(value = 100, message = "每页条数不能超过100")
  private Integer pageSize = 10;
  private String orderNumber;
  private Integer applyStatus;
  private Integer refundState;
}
