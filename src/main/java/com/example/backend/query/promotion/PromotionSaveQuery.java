package com.example.backend.query.promotion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PromotionSaveQuery {

  private Integer id;

  @NotNull(message = "{validator.promotion.cinemaId.required}")
  private Integer cinemaId;

  @NotEmpty(message = "{validator.promotion.name.required}")
  private String name;

  private String remark;

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
  }

  @Data
  public static class WeeklyDayItem {
    @NotEmpty(message = "{validator.promotion.weeklyDay.name.required}")
    private String name;

    @NotNull(message = "{validator.promotion.weeklyDay.weekday.required}")
    private Integer weekday;

    @NotNull(message = "{validator.promotion.weeklyDay.price.required}")
    private Integer price;
  }

  @Data
  public static class SpecificDateItem {
    @NotEmpty(message = "{validator.promotion.specificDate.name.required}")
    private String name;

    @NotEmpty(message = "{validator.promotion.specificDate.date.required}")
    private String date;

    @NotNull(message = "{validator.promotion.specificDate.price.required}")
    private Integer price;
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
  }
}
