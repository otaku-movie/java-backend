package com.example.backend.response.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionDetailResponse {

  private Integer promotionId;
  private String name;
  private String remark;
  /** 是否支持前售券(ムビチケ)类抵扣 */
  private Boolean allowMuviticket;
  /** 促销优先级 */
  private Integer priority;
  /** 规则类型优先级：月度/周度/固定日/时段/固定票价/票种规则，数值越小越优先 */
  private Integer monthlyPriority;
  private Integer weeklyPriority;
  private Integer specificDatePriority;
  private Integer timeRangePriority;
  private Integer fixedPricePriority;
  private Integer ticketTypePriority;
  /** 定价规则（人群+票价+优先级），用于系统活动模式 */
  private List<PricingRuleItem> pricingRules;
  private List<MonthlyDayItem> monthlyDays;
  private List<WeeklyDayItem> weeklyDays;
  private List<SpecificDateItem> specificDates;
  private List<TimeRangeItem> timeRanges;

  @Data
  public static class PricingRuleItem {
    private Integer id;
    private Integer audienceType;
    private BigDecimal value;
    private Integer priority;
  }

  @Data
  public static class MonthlyDayItem {
    private Integer id;
    private String name;
    private Integer dayOfMonth;
    private Integer price;
    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class WeeklyDayItem {
    private Integer id;
    private String name;
    private Integer weekday;
    private Integer price;
    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class SpecificDateItem {
    private Integer id;
    private String name;
    private String date;
    private Integer price;
    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class TimeRangeItem {
    private Integer id;
    private String name;
    private String applicableScope;
    private String applicableDays;
    private String startTime;
    private String endTime;
    private Integer price;
    private String remark;
    private Integer priority;
    private Boolean enabled;
  }
}
