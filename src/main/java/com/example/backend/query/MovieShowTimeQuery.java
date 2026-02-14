package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MovieShowTimeQuery {
  Integer id;
  @NotNull
  Integer cinemaId;
  @NotNull
  Integer theaterHallId;
  @NotNull
  Integer movieId;
  Boolean open;
  @NotNull
  String startTime;
  @NotNull
  String endTime;
  List<Integer> specIds;
  /** 放映类型 dict_item.id 或 code (2D/3D) */
  Integer dimensionType;
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  Integer movieVersionId;
  /** 定价模式：1=系统活动模式 2=固定价格模式 */
  Integer pricingMode;
  /** 固定价格模式下的基础票价，pricing_mode=2 时使用 */
  BigDecimal fixedAmount;
  /** 规格补价(3D/IMAX等) */
  BigDecimal surcharge;
  /** 是否支持前售券(ムビチケ等) */
  Boolean allowPresale;

  /** 定时公开时间（yyyy-MM-dd HH:mm:ss，null 表示立即公开） */
  String publishAt;

  /** 开放购票时间（yyyy-MM-dd HH:mm:ss） */
  String saleOpenAt;

  /** 默认规则下，本场次票种临时调价：票种 id -> 覆盖价格（未覆盖用票种默认价） */
  Map<Integer, BigDecimal> ticketTypeOverrides;
  /** 默认规则下，本场次票种是否启用：票种 id -> 是否启用（仅限该场次，默认 true） */
  Map<Integer, Boolean> ticketTypeEnabled;
}
