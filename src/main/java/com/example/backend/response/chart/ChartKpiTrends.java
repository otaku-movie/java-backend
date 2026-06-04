package com.example.backend.response.chart;

import lombok.Data;

/**
 * Dashboard 顶部 KPI 环比（相对昨日）。
 *
 * <p>场次用百分比（今日 vs 昨日同日场次数）；电影 / 影院用绝对增量
 * （今日 0 点前已存在 vs 当前总量之差 = 今日新增数）。
 */
@Data
public class ChartKpiTrends {
  /** 今日场次相对昨日的变化率（%），昨日为 0 时返回 null。 */
  Double todayShowTimeChangePercent;
  /** 今日新增电影数（create_time 日期 = 今天）。 */
  Long movieCountChange;
  /** 今日新增影院数。 */
  Long cinemaCountChange;
}
