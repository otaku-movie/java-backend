package com.example.backend.query.refund;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundProcessQuery {
  @NotNull(message = "退款ID不能为空")
  private Integer id;
  /** true=同意, false=拒绝 */
  private Boolean approved = true;
  /** 拒绝原因（拒绝时填写） */
  private String rejectReason;
}
