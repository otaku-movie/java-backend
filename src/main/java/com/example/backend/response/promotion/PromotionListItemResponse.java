package com.example.backend.response.promotion;

import com.example.backend.response.promotion.PromotionDetailResponse.MonthlyDayItem;
import com.example.backend.response.promotion.PromotionDetailResponse.SpecificDateItem;
import com.example.backend.response.promotion.PromotionDetailResponse.TimeRangeItem;
import com.example.backend.response.promotion.PromotionDetailResponse.WeeklyDayItem;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PromotionListItemResponse {
  private Integer id;
  private Integer cinemaId;
  private String name;
  private String remark;
  private Integer priority;
  /** 规则类型优先级：月度/周度/固定日/时段/固定票价/票种规则 */
  private Integer monthlyPriority;
  private Integer weeklyPriority;
  private Integer specificDatePriority;
  private Integer timeRangePriority;
  private Integer fixedPricePriority;
  private Integer ticketTypePriority;
  private Date createTime;
  private Date updateTime;
  private List<MonthlyDayItem> monthlyDays;
  private List<WeeklyDayItem> weeklyDays;
  private List<SpecificDateItem> specificDates;
  private List<TimeRangeItem> timeRanges;
}
