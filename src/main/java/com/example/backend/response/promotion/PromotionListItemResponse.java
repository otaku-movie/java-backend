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
  private Date createTime;
  private Date updateTime;
  private List<MonthlyDayItem> monthlyDays;
  private List<WeeklyDayItem> weeklyDays;
  private List<SpecificDateItem> specificDates;
  private List<TimeRangeItem> timeRanges;
}
