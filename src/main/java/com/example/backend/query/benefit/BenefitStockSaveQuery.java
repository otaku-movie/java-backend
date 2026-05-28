package com.example.backend.query.benefit;

import lombok.Data;

@Data
public class BenefitStockSaveQuery {
  private Integer id;
  /** 电影 ID（新建时与 benefitId 一起使用，后端据此解析物料） */
  private Integer movieId;
  /** 特典阶段 ID（新建时必填；后端按阶段下首个物料建库存，无则自动建默认物料） */
  private Integer benefitId;
  /**
   * 影院 ID：新建库存时必填；按 {@link #id} 更新已存在记录时可不传（沿用库里的 cinema_id）。
   */
  private Integer cinemaId;
  /** 分配的特典数量（配额），不填表示库存未知 */
  private Integer quota;
  /** 剩余数量，不传则等于 quota（新建时）或保持原值（更新时）；不填可表示未知 */
  private Integer remaining;
  /** 1=运营置为已领完 */
  private Integer manualSoldOut;
}
