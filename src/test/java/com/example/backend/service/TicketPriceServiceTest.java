package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.CinemaPriceRulesConfigMapper;
import com.example.backend.mapper.CinemaSpecSpecMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.mapper.PricingRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketPriceServiceTest {

  @Mock
  private MovieTicketTypeMapper movieTicketTypeMapper;
  @Mock
  private CinemaPriceConfigMapper cinemaPriceConfigMapper;
  @Mock
  private CinemaSpecSpecMapper cinemaSpecSpecMapper;
  @Mock
  private MovieShowTimeMapper movieShowTimeMapper;
  @Mock
  private CinemaPriceRulesConfigMapper configMapper;
  @Mock
  private PricingRuleMapper pricingRuleMapper;

  @InjectMocks
  private TicketPriceService service;

  @Test
  void getSurchargeForShowtime_adds3dAndSpecPlusPrice() {
    MovieShowTime st = new MovieShowTime();
    st.setCinemaId(10);
    st.setDimensionType(2);
    st.setSpecIds(List.of(3, 7));

    when(cinemaPriceConfigMapper.getSurcharge(10, 2)).thenReturn(new BigDecimal("400"));

    CinemaSpecSpec imax = new CinemaSpecSpec();
    imax.setPlusPrice(500);
    CinemaSpecSpec dolby = new CinemaSpecSpec();
    dolby.setPlusPrice(300);
    when(cinemaSpecSpecMapper.selectOne(any(QueryWrapper.class)))
        .thenReturn(imax)
        .thenReturn(dolby);

    BigDecimal total = service.getSurchargeForShowtime(st);

    assertEquals(new BigDecimal("1200"), total);
  }

  @Test
  void getSurchargeForShowtime_usesShowtimeOverrideWhenSet() {
    MovieShowTime st = new MovieShowTime();
    st.setCinemaId(10);
    st.setSurcharge(new BigDecimal("999"));
    st.setSpecIds(List.of(3));

    assertEquals(new BigDecimal("999"), service.getSurchargeForShowtime(st));
  }
}
