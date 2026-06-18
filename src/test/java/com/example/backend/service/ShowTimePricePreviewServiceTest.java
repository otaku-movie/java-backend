package com.example.backend.service;

import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieShowTimeTicketTypeMapper;
import com.example.backend.response.showTime.ShowTimePricePreviewResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowTimePricePreviewServiceTest {

  @Mock
  private MovieShowTimeMapper movieShowTimeMapper;
  @Mock
  private MovieTicketTypeService movieTicketTypeService;
  @Mock
  private TicketPriceService ticketPriceService;
  @Mock
  private MovieShowTimeTicketTypeMapper movieShowTimeTicketTypeMapper;

  @InjectMocks
  private ShowTimePricePreviewService service;

  @Test
  void preview_usesBaseTicketPlusSurcharge() {
    MovieShowTime st = new MovieShowTime();
    st.setId(1);
    st.setCinemaId(10);
    st.setPricingMode(0);
    when(movieShowTimeMapper.selectById(1)).thenReturn(st);
    when(ticketPriceService.getSurchargeForShowtime(st)).thenReturn(new BigDecimal("500"));
    when(movieShowTimeTicketTypeMapper.selectList(any())).thenReturn(List.of());

    MovieTicketType general = ticket(101, "一般", new BigDecimal("2000"), null, 0);
    general.setAudienceCategory("adult");
    general.setPriceKind("base");
    MovieTicketType wednesday = ticket(102, "TOHOウェンズデイ", new BigDecimal("1300"), 1, 10);
    wednesday.setAudienceCategory("adult");
    wednesday.setPriceKind("promo");
    wednesday.setServiceDayCode("wednesday");
    MovieTicketType member = ticket(103, "TOHO-ONEメンバーデイ", new BigDecimal("1300"), 1, 11);
    member.setPriceKind("promo");
    member.setServiceDayCode("tuesday_member");
    member.setMemberRequired(true);
    when(movieTicketTypeService.listByShowtime(1)).thenReturn(List.of(general, wednesday, member));

    ShowTimePricePreviewResponse out = service.preview(1);

    assertEquals(new BigDecimal("2500"), out.getReferencePrice());
    assertEquals(new BigDecimal("1800"), out.getPromoFromPrice());
    assertEquals("wednesday", out.getPromoLabel());
    assertEquals(new BigDecimal("1800"), out.getMemberFromPrice());
  }

  @Test
  void preview_excludesDisabilityAndSeniorFromPromo() {
    MovieShowTime st = new MovieShowTime();
    st.setId(5);
    st.setCinemaId(10);
    st.setPricingMode(0);
    when(movieShowTimeMapper.selectById(5)).thenReturn(st);
    when(ticketPriceService.getSurchargeForShowtime(st)).thenReturn(BigDecimal.ZERO);
    when(movieShowTimeTicketTypeMapper.selectList(any())).thenReturn(List.of());

    MovieTicketType general = ticket(401, "一般", new BigDecimal("2000"), null, 0);
    general.setAudienceCategory("adult");
    general.setPriceKind("base");
    MovieTicketType wednesday = ticket(402, "TOHOウェンズデイ", new BigDecimal("1300"), 1, 10);
    wednesday.setAudienceCategory("adult");
    wednesday.setPriceKind("promo");
    wednesday.setServiceDayCode("wednesday");
    MovieTicketType disability = ticket(403, "障がい者割引", new BigDecimal("1000"), null, 20);
    disability.setAudienceCategory("adult");
    disability.setPriceKind("promo");
    disability.setServiceDayCode("other");
    MovieTicketType senior = ticket(404, "シニア割引", new BigDecimal("1300"), null, 21);
    senior.setAudienceCategory("adult");
    senior.setPriceKind("promo");
    senior.setServiceDayCode("other");
    when(movieTicketTypeService.listByShowtime(5)).thenReturn(
        List.of(general, wednesday, disability, senior));

    ShowTimePricePreviewResponse out = service.preview(5);

    assertEquals(new BigDecimal("2000"), out.getReferencePrice());
    assertEquals(new BigDecimal("1300"), out.getPromoFromPrice());
    assertEquals("wednesday", out.getPromoLabel());
  }

  @Test
  void preview_legacyNameFallbackWhenSemanticsMissing() {
    MovieShowTime st = new MovieShowTime();
    st.setId(3);
    st.setPricingMode(0);
    when(movieShowTimeMapper.selectById(3)).thenReturn(st);
    when(ticketPriceService.getSurchargeForShowtime(st)).thenReturn(BigDecimal.ZERO);
    when(movieShowTimeTicketTypeMapper.selectList(any())).thenReturn(List.of());

    MovieTicketType general = ticket(201, "一般", new BigDecimal("1900"), null, 0);
    MovieTicketType member = ticket(202, "ムービーウォーカー会員", new BigDecimal("1500"), null, 5);
    when(movieTicketTypeService.listByShowtime(3)).thenReturn(List.of(general, member));

    ShowTimePricePreviewResponse out = service.preview(3);

    assertEquals(new BigDecimal("1900"), out.getReferencePrice());
    assertEquals(new BigDecimal("1500"), out.getMemberFromPrice());
  }

  @Test
  void preview_includesSpecSurchargeFromTicketPriceService() {
    MovieShowTime st = new MovieShowTime();
    st.setId(4);
    st.setCinemaId(10);
    st.setPricingMode(0);
    st.setDimensionType(2);
    st.setSpecIds(List.of(3));
    when(movieShowTimeMapper.selectById(4)).thenReturn(st);
    when(ticketPriceService.getSurchargeForShowtime(st)).thenReturn(new BigDecimal("900"));
    when(movieShowTimeTicketTypeMapper.selectList(any())).thenReturn(List.of());

    MovieTicketType general = ticket(301, "一般", new BigDecimal("2000"), null, 0);
    general.setAudienceCategory("adult");
    general.setPriceKind("base");
    when(movieTicketTypeService.listByShowtime(4)).thenReturn(List.of(general));

    ShowTimePricePreviewResponse out = service.preview(4);

    assertEquals(new BigDecimal("2900"), out.getReferencePrice());
  }

  @Test
  void preview_fixedModeUsesFixedAmount() {
    MovieShowTime st = new MovieShowTime();
    st.setId(2);
    st.setPricingMode(2);
    st.setFixedAmount(new BigDecimal("1500"));
    when(movieShowTimeMapper.selectById(2)).thenReturn(st);
    when(ticketPriceService.getSurchargeForShowtime(st)).thenReturn(new BigDecimal("300"));

    ShowTimePricePreviewResponse out = service.preview(2);

    assertEquals(new BigDecimal("1500"), out.getFixedAmount());
    assertEquals(new BigDecimal("1800"), out.getReferencePrice());
  }

  private static MovieTicketType ticket(int id, String name, BigDecimal price, Integer scheduleType, int order) {
    MovieTicketType t = new MovieTicketType();
    t.setId(id);
    t.setName(name);
    t.setPrice(price);
    t.setScheduleType(scheduleType);
    t.setOrderNum(order);
    return t;
  }
}
