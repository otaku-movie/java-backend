package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieShowTimeTicketType;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieShowTimeTicketTypeMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 票种查询：按影院列表、按场次可用列表。
 */
@Service
public class MovieTicketTypeService {

  @Autowired
  private MovieTicketTypeMapper movieTicketTypeMapper;
  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;
  @Autowired
  private MovieShowTimeTicketTypeMapper movieShowTimeTicketTypeMapper;

  /**
   * 按影院查询票种列表，可选按星期/日期/时段过滤。
   *
   * @param cinemaId      影院 ID，必填
   * @param weekday       1=周一…7=周日，null 表示不按星期过滤
   * @param targetDate     YYYY-MM-DD，与 weekday 配合
   * @param startTime      HH:mm，与 endTime 一起传入时过滤 scheduleType=每日 的票种
   * @param endTime        HH:mm
   * @param includeDisabled true 时返回全部（含已禁用），仅管理端票种管理页用；false 时只返回启用的
   */
  public List<MovieTicketType> listByCinema(Integer cinemaId, Integer weekday,
                                            String targetDate, String startTime, String endTime,
                                            boolean includeDisabled) {
    if (cinemaId == null) {
      return Collections.emptyList();
    }
    QueryWrapper<MovieTicketType> wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", cinemaId);
    if (!includeDisabled) {
      wrapper.and(w -> w.isNull("enabled").or().eq("enabled", true));
    }
    wrapper.orderByAsc("order_num", "create_time");
    List<MovieTicketType> list = movieTicketTypeMapper.selectList(wrapper);
    if (weekday != null && weekday >= 1 && weekday <= 7) {
      list = list.stream()
          .filter(t -> isApplicableOnDate(t, weekday, targetDate))
          .collect(Collectors.toList());
    }
    if (startTime != null && endTime != null) {
      list = list.stream()
          .filter(t -> isApplicableInTimeRange(t, startTime, endTime))
          .collect(Collectors.toList());
    }
    return list;
  }

  /**
   * 按场次查询该场次可用的票种（App 选票页调用，返回时已排除本场次禁用的票种）。
   * 若场次在 movie_show_time_ticket_type 有任意配置：先按影院+日期时段取适用票种，再排除配置中 enabled=false 的；
   * 无配置时按场次日期与时段过滤影院票种返回。
   */
  public List<MovieTicketType> listByShowtime(Integer movieShowTimeId) {
    if (movieShowTimeId == null) {
      return Collections.emptyList();
    }
    MovieShowTime showTime = movieShowTimeMapper.selectById(movieShowTimeId);
    if (showTime == null) {
      return Collections.emptyList();
    }
    Integer cinemaId = showTime.getCinemaId();
    List<MovieShowTimeTicketType> configs = movieShowTimeTicketTypeMapper.selectList(
        new QueryWrapper<MovieShowTimeTicketType>()
            .eq("show_time_id", movieShowTimeId));
    int w = 0;
    String targetDate = null;
    String startHm = null;
    String endHm = null;
    String startStr = showTime.getStartTime();
    String endStr = showTime.getEndTime();
    if (startStr != null && startStr.length() >= 10) {
      targetDate = startStr.substring(0, 10);
      if (startStr.length() >= 16) {
        startHm = startStr.substring(11, 16);
      } else if (startStr.length() >= 13) {
        startHm = startStr.substring(11);
      }
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        Date d = sdf.parse(targetDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int j = cal.get(Calendar.DAY_OF_WEEK);
        w = (j == Calendar.SUNDAY) ? 7 : j - 1;
      } catch (ParseException ignored) {}
    }
    if (endStr != null && endStr.length() >= 16) {
      endHm = endStr.substring(11, 16);
    } else if (endStr != null && endStr.length() >= 13) {
      endHm = endStr.substring(11);
    }
    Integer weekday = (w >= 1 && w <= 7) ? w : null;
    List<MovieTicketType> list = listByCinema(cinemaId, weekday, targetDate, startHm, endHm, false);
    if (configs != null && !configs.isEmpty()) {
      Set<Integer> disabledTypeIds = configs.stream()
          .filter(c -> Boolean.FALSE.equals(c.getEnabled()))
          .map(MovieShowTimeTicketType::getTicketTypeId)
          .collect(Collectors.toSet());
      if (!disabledTypeIds.isEmpty()) {
        list = list.stream()
            .filter(t -> t.getId() != null && !disabledTypeIds.contains(t.getId()))
            .collect(Collectors.toList());
      }
    }
    return list;
  }

  private static boolean isApplicableOnDate(MovieTicketType t, int weekday, String targetDate) {
    Integer st = t.getScheduleType();
    if (st == null) return true;
    switch (st) {
      case 1:
        List<Integer> weekdays = t.getApplicableWeekdays();
        if (weekdays == null || weekdays.isEmpty()) return true;
        return weekdays.contains(weekday);
      case 2:
        if (targetDate == null || targetDate.length() < 10) return true;
        List<Integer> monthDays = t.getApplicableMonthDays();
        if (monthDays == null || monthDays.isEmpty()) return true;
        try {
          int day = Integer.parseInt(targetDate.substring(8, 10));
          return monthDays.contains(day);
        } catch (Exception e) {
          return true;
        }
      case 3:
        return true;
      case 4:
        if (targetDate == null) return true;
        List<String> dates = t.getApplicableDates();
        if (dates == null || dates.isEmpty()) return true;
        return dates.contains(targetDate);
      default:
        return true;
    }
  }

  private static boolean isApplicableInTimeRange(MovieTicketType t, String showStart, String showEnd) {
    if (t.getScheduleType() == null || t.getScheduleType() != 3) return true;
    String dailyStart = t.getDailyStartTime();
    String dailyEnd = t.getDailyEndTime();
    if (dailyStart == null || dailyStart.isBlank() || dailyEnd == null || dailyEnd.isBlank()) return true;
    int showStartMin = parseTimeToMinutes(showStart);
    int showEndMin = parseTimeToMinutes(showEnd);
    int dailyStartMin = parseTimeToMinutes(dailyStart);
    int dailyEndMin = parseTimeToMinutes(dailyEnd);
    if (showStartMin < 0 || showEndMin < 0 || dailyStartMin < 0 || dailyEndMin < 0) return true;
    boolean dailyOvernight = dailyEndMin <= dailyStartMin;
    boolean showOvernight = showEndMin <= showStartMin;
    if (!dailyOvernight && !showOvernight) {
      return showStartMin < dailyEndMin && showEndMin > dailyStartMin;
    }
    if (dailyOvernight && !showOvernight) {
      return showStartMin < dailyEndMin || showEndMin > dailyStartMin;
    }
    if (!dailyOvernight && showOvernight) {
      return showStartMin < dailyEndMin || showEndMin > dailyStartMin;
    }
    return showStartMin >= dailyStartMin || showEndMin <= dailyEndMin;
  }

  private static int parseTimeToMinutes(String time) {
    if (time == null || time.isBlank()) return -1;
    String trimmed = time.trim();
    if (trimmed.length() < 4) return -1;
    String[] parts = trimmed.split(":");
    try {
      int h = Integer.parseInt(parts[0].trim());
      int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
      if (h < 0 || h > 24 || m < 0 || m > 59) return -1;
      return (h % 24) * 60 + m;
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
