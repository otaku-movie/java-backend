package com.example.backend.query.promotion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionSaveQuery {

  private Integer id;

  @NotNull(message = "{validator.promotion.cinemaId.required}")
  private Integer cinemaId;

  /** 活动名称（已废弃，规则平铺后不再使用，保留兼容） */
  private String name;

  private String remark;

  /** 是否支持前售券(ムビチケ)类抵扣 */
  private Boolean allowMuviticket;

  /** 定价规则（人群+票价+优先级） */
  private List<PricingRuleItem> pricingRules;

  /** 促销优先级（多促销时排序） */
  private Integer priority;
  /** 规则类型优先级：月度/周度/固定日/时段/固定票价/票种规则，数值越小越优先，未填默认 0 */
  private Integer monthlyPriority;
  private Integer weeklyPriority;
  private Integer specificDatePriority;
  private Integer timeRangePriority;
  private Integer fixedPricePriority;
  private Integer ticketTypePriority;

  @Valid
  private List<MonthlyDayItem> monthlyDays;

  @Valid
  private List<WeeklyDayItem> weeklyDays;

  @Valid
  private List<SpecificDateItem> specificDates;

  @Valid
  private List<TimeRangeItem> timeRanges;

  @Data
  public static class MonthlyDayItem {
    @NotEmpty(message = "{validator.promotion.monthlyDay.name.required}")
    private String name;

    @NotNull(message = "{validator.promotion.monthlyDay.day.required}")
    private Integer dayOfMonth;

    @NotNull(message = "{validator.promotion.monthlyDay.price.required}")
    private Integer price;

    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class WeeklyDayItem {
    @NotEmpty(message = "{validator.promotion.weeklyDay.name.required}")
    private String name;

    @NotNull(message = "{validator.promotion.weeklyDay.weekday.required}")
    private Integer weekday;

    @NotNull(message = "{validator.promotion.weeklyDay.price.required}")
    private Integer price;

    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class SpecificDateItem {
    @NotEmpty(message = "{validator.promotion.specificDate.name.required}")
    private String name;

    @NotEmpty(message = "{validator.promotion.specificDate.date.required}")
    private String date;

    @NotNull(message = "{validator.promotion.specificDate.price.required}")
    private Integer price;

    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class TimeRangeItem {
    @NotEmpty(message = "{validator.promotion.timeRange.name.required}")
    private String name;

    private String applicableScope;

    private String applicableDays;

    @NotEmpty(message = "{validator.promotion.timeRange.startTime.required}")
    private String startTime;

    @NotEmpty(message = "{validator.promotion.timeRange.endTime.required}")
    private String endTime;

    @NotNull(message = "{validator.promotion.timeRange.price.required}")
    private Integer price;

    private String remark;
    private Integer priority;
    private Boolean enabled;
  }

  @Data
  public static class PricingRuleItem {
    private Integer id;
    private Integer audienceType;
    private BigDecimal value;
    private Integer priority;
  }
}
