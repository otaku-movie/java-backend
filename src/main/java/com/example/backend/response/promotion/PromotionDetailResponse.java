package com.example.backend.response.promotion;

import lombok.Data;

import java.util.List;

@Data
public class PromotionDetailResponse {

  private Integer promotionId;
  private String name;
  private String remark;
  private List<MonthlyDayItem> monthlyDays;
  private List<WeeklyDayItem> weeklyDays;
  private List<SpecificDateItem> specificDates;
  private List<TimeRangeItem> timeRanges;

  @Data
  public static class MonthlyDayItem {
    private Integer id;
    private String name;
    private Integer dayOfMonth;
    private Integer price;
  }

  @Data
  public static class WeeklyDayItem {
    private Integer id;
    private String name;
    private Integer weekday;
    private Integer price;
  }

  @Data
  public static class SpecificDateItem {
    private Integer id;
    private String name;
    private String date;
    private Integer price;
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
  }
}
