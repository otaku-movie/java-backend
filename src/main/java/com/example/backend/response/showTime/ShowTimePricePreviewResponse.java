package com.example.backend.response.showTime;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 场次票价预览（列表参考价，非下单权威价）。
 */
@Data
public class ShowTimePricePreviewResponse {
  private Integer movieShowTimeId;
  /** 定价模式：0 默认票种 / 1 活动规则 / 2 固定价 */
  private Integer pricingMode;
  /** 固定价模式基础价（未含规格加价） */
  private BigDecimal fixedAmount;
  /** 公众参考价（基准票种或固定价 + 本场规格/3D 加价，不含座位区域价） */
  private BigDecimal referencePrice;
  /** 本场非会员适用票种最低价 + 加价；低于 reference 时可用于优惠标签 */
  private BigDecimal promoFromPrice;
  /** 达成 promoFromPrice 的票种简称 */
  private String promoLabel;
  /** 本场会员票种最低价 + 加价；无会员票种时为 null */
  private BigDecimal memberFromPrice;
}
