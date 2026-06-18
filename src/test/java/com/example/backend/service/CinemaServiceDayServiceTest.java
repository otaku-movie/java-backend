package com.example.backend.service;

import com.example.backend.entity.MovieTicketType;
import com.example.backend.response.showTime.ServiceDayResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceDayServiceTest {

  @Mock
  private MovieTicketTypeService movieTicketTypeService;

  @InjectMocks
  private CinemaServiceDayService service;

  @Test
  void listForDate_matchesCalendarServiceDaysOnly() {
    MovieTicketType wednesday = promo(1, "TOHOウェンズデイ", "wednesday", 1);
    MovieTicketType pontaWed = memberPromo(4, "Pontaパス ウェンズデイ", "wednesday_member", 1);
    MovieTicketType late = promo(2, "レイトショー", "late_show", 3);
    MovieTicketType disability = promo(3, "障がい者割引", "other", null);
    disability.setAudienceCategory("adult");

    when(movieTicketTypeService.listByCinema(eq(10), eq(3), eq("2026-06-17"), isNull(), isNull(), eq(false)))
        .thenReturn(List.of(wednesday, pontaWed, late, disability));

    List<ServiceDayResponse> out = service.listForDate(10, "2026-06-17");

    assertEquals(2, out.size());
    assertEquals("wednesday", out.get(0).getCode());
    assertEquals("TOHOウェンズデイ", out.get(0).getName());
    assertEquals("wednesday_member", out.get(1).getCode());
    assertEquals(Boolean.TRUE, out.get(1).getMember());
  }

  @Test
  void listForDate_includesThursdayMemberDay() {
    MovieTicketType saisonThu = memberPromo(5, "セゾンの木曜日", "thursday_member", 1);

    when(movieTicketTypeService.listByCinema(eq(20), eq(4), eq("2026-06-18"), isNull(), isNull(), eq(false)))
        .thenReturn(List.of(saisonThu));

    List<ServiceDayResponse> out = service.listForDate(20, "2026-06-18");

    assertEquals(1, out.size());
    assertEquals("セゾンの木曜日", out.get(0).getName());
    assertEquals(Boolean.TRUE, out.get(0).getMember());
  }

  @Test
  void weekdayFromDate_usesMonToSunOneToSeven() {
    assertEquals(3, CinemaServiceDayService.weekdayFromDate("2026-06-17"));
    assertEquals(7, CinemaServiceDayService.weekdayFromDate("2026-06-21"));
  }

  @Test
  void listForDate_returnsEmptyWhenDateInvalid() {
    assertTrue(service.listForDate(10, "bad-date").isEmpty());
  }

  private static MovieTicketType promo(int id, String name, String code, Integer scheduleType) {
    MovieTicketType t = new MovieTicketType();
    t.setId(id);
    t.setName(name);
    t.setPriceKind("promo");
    t.setAudienceCategory("adult");
    t.setServiceDayCode(code);
    t.setScheduleType(scheduleType);
    t.setOrderNum(id);
    return t;
  }

  private static MovieTicketType memberPromo(int id, String name, String code, Integer scheduleType) {
    MovieTicketType t = promo(id, name, code, scheduleType);
    t.setMemberRequired(true);
    return t;
  }
}
