package com.example.backend.service;

import com.example.backend.entity.MovieTicketType;
import com.example.backend.response.showTime.ServiceDayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按影院 + 日历日期匹配当日服务日（水曜・会员木曜等），不考虑场次时段与票价。
 */
@Service
@RequiredArgsConstructor
public class CinemaServiceDayService {

  private static final List<String> CODE_ORDER = List.of(
      "wednesday",
      "first_day",
      "movie_day",
      "saturday_morning",
      "weekly_other",
      "monthly_other",
      "monday_member",
      "tuesday_member",
      "wednesday_member",
      "thursday_member",
      "friday_member",
      "couple_50",
      "pair_50",
      "other"
  );

  private final MovieTicketTypeService movieTicketTypeService;

  public List<ServiceDayResponse> listForDate(Integer cinemaId, String date) {
    if (cinemaId == null || date == null || date.isBlank()) {
      return List.of();
    }
    Integer weekday = weekdayFromDate(date);
    if (weekday == null) {
      return List.of();
    }
    List<MovieTicketType> types = movieTicketTypeService.listByCinema(
        cinemaId, weekday, date, null, null, false);
    Map<String, ServiceDayResponse> byCode = new LinkedHashMap<>();
    Map<String, Integer> orderByCode = new HashMap<>();
    for (MovieTicketType t : types) {
      if (!TicketTypeSemantics.isCalendarServiceDay(t)) {
        continue;
      }
      String code = resolveCode(t);
      if (code == null) {
        continue;
      }
      int ord = t.getOrderNum() != null ? t.getOrderNum() : Integer.MAX_VALUE;
      if (!byCode.containsKey(code) || ord < orderByCode.getOrDefault(code, Integer.MAX_VALUE)) {
        byCode.put(code, toResponse(t, code));
        orderByCode.put(code, ord);
      }
    }
    List<ServiceDayResponse> out = new ArrayList<>(byCode.values());
    out.sort(Comparator
        .comparingInt((ServiceDayResponse r) -> orderByCode.getOrDefault(r.getCode(), Integer.MAX_VALUE))
        .thenComparingInt(r -> codeRank(r.getCode())));
    return out;
  }

  private static String resolveCode(MovieTicketType t) {
    String code = t.getServiceDayCode();
    if (code != null && !code.isBlank()) {
      return code.trim();
    }
    return null;
  }

  private static ServiceDayResponse toResponse(MovieTicketType t, String code) {
    ServiceDayResponse r = new ServiceDayResponse();
    r.setCode(code);
    String name = t.getName();
    if (name != null && !name.isBlank()) {
      r.setName(name.trim());
    }
    r.setMember(TicketTypeSemantics.isMemberRequired(t));
    return r;
  }

  private static int codeRank(String code) {
    if (code == null) {
      return CODE_ORDER.size();
    }
    int i = CODE_ORDER.indexOf(code);
    return i >= 0 ? i : CODE_ORDER.size();
  }

  /** 1=周一 … 7=周日，与 movie_ticket_type.applicable_weekdays 一致。 */
  static Integer weekdayFromDate(String date) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setLenient(false);
      Date d = sdf.parse(date);
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      int j = cal.get(Calendar.DAY_OF_WEEK);
      return (j == Calendar.SUNDAY) ? 7 : j - 1;
    } catch (ParseException e) {
      return null;
    }
  }
}
