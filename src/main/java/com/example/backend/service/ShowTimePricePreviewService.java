package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieShowTimeTicketType;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieShowTimeTicketTypeMapper;
import com.example.backend.response.showTime.ShowTimePricePreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 场次票价预览：供 H5 场次列表展示参考价与会员价标记（不选票种）。
 */
@Service
@RequiredArgsConstructor
public class ShowTimePricePreviewService {

  private static final int BATCH_MAX = 50;

  /** 旧数据无 audience_category / member_required 时的名称兜底。 */
  private static final Pattern LEGACY_MEMBER_NAME = Pattern.compile(
      "会員|会员|メンバー|プラス会員|TOHO-ONE|Ponta|ポンタ|チネクラブ|ムービーウォーカー|mopia",
      Pattern.CASE_INSENSITIVE);

  private final MovieShowTimeMapper movieShowTimeMapper;
  private final MovieTicketTypeService movieTicketTypeService;
  private final TicketPriceService ticketPriceService;
  private final MovieShowTimeTicketTypeMapper movieShowTimeTicketTypeMapper;

  public ShowTimePricePreviewResponse preview(Integer movieShowTimeId) {
    ShowTimePricePreviewResponse out = new ShowTimePricePreviewResponse();
    out.setMovieShowTimeId(movieShowTimeId);
    if (movieShowTimeId == null) {
      return out;
    }
    MovieShowTime showtime = movieShowTimeMapper.selectById(movieShowTimeId);
    if (showtime == null) {
      return out;
    }
    Integer pricingMode = showtime.getPricingMode();
    out.setPricingMode(pricingMode);
    BigDecimal surcharge = ticketPriceService.getSurchargeForShowtime(showtime);

    if (pricingMode != null && pricingMode == TicketPriceService.PRICING_MODE_FIXED
        && showtime.getFixedAmount() != null) {
      out.setFixedAmount(showtime.getFixedAmount());
      out.setReferencePrice(showtime.getFixedAmount().add(surcharge));
      return out;
    }

    List<MovieTicketType> types = movieTicketTypeService.listByShowtime(movieShowTimeId);
    if (types == null || types.isEmpty()) {
      return out;
    }

    Map<Integer, BigDecimal> overrides = loadOverridePrices(movieShowTimeId);

    MovieTicketType referenceType = types.stream()
        .filter(TicketTypeSemantics::isAdultReferenceCandidate)
        .min(referenceComparator(overrides))
        .or(() -> types.stream()
            .filter(TicketTypeSemantics::isBaseKind)
            .min(referenceComparator(overrides)))
        .orElse(null);

    if (referenceType != null) {
      BigDecimal refBase = effectiveBase(referenceType, overrides);
      if (refBase != null) {
        out.setReferencePrice(refBase.add(surcharge));
      }
    }

    BigDecimal bestPromo = null;
    String bestPromoLabel = null;
    BigDecimal bestMember = null;

    for (MovieTicketType t : types) {
      BigDecimal base = effectiveBase(t, overrides);
      if (base == null) {
        continue;
      }
      BigDecimal total = base.add(surcharge);
      if (isMemberTicket(t)) {
        if (bestMember == null || total.compareTo(bestMember) < 0) {
          bestMember = total;
        }
      } else if (TicketTypeSemantics.isAdultPublicPromo(t)) {
        if (bestPromo == null || total.compareTo(bestPromo) < 0) {
          bestPromo = total;
          bestPromoLabel = promoLabel(t);
        }
      }
    }

    out.setMemberFromPrice(bestMember);
    if (bestPromo != null && out.getReferencePrice() != null
        && bestPromo.compareTo(out.getReferencePrice()) < 0) {
      out.setPromoFromPrice(bestPromo);
      out.setPromoLabel(bestPromoLabel);
    } else if (bestPromo != null && out.getReferencePrice() == null) {
      out.setReferencePrice(bestPromo);
    }

    return out;
  }

  public List<ShowTimePricePreviewResponse> previewBatch(List<Integer> movieShowTimeIds) {
    if (movieShowTimeIds == null || movieShowTimeIds.isEmpty()) {
      return List.of();
    }
    return movieShowTimeIds.stream()
        .filter(Objects::nonNull)
        .distinct()
        .limit(BATCH_MAX)
        .map(this::preview)
        .collect(Collectors.toList());
  }

  private static Comparator<MovieTicketType> referenceComparator(Map<Integer, BigDecimal> overrides) {
    return Comparator
        .comparing((MovieTicketType t) -> t.getOrderNum() != null ? t.getOrderNum() : Integer.MAX_VALUE)
        .thenComparing(t -> effectiveBase(t, overrides), Comparator.nullsLast(BigDecimal::compareTo));
  }

  private Map<Integer, BigDecimal> loadOverridePrices(Integer showtimeId) {
    List<MovieShowTimeTicketType> configs = movieShowTimeTicketTypeMapper.selectList(
        new QueryWrapper<MovieShowTimeTicketType>().eq("show_time_id", showtimeId));
    Map<Integer, BigDecimal> map = new HashMap<>();
    if (configs == null) {
      return map;
    }
    for (MovieShowTimeTicketType c : configs) {
      if (c.getTicketTypeId() != null && c.getOverridePrice() != null) {
        map.put(c.getTicketTypeId(), c.getOverridePrice());
      }
    }
    return map;
  }

  private static BigDecimal effectiveBase(MovieTicketType t, Map<Integer, BigDecimal> overrides) {
    if (t.getId() != null && overrides.containsKey(t.getId())) {
      return overrides.get(t.getId());
    }
    return t.getPrice();
  }

  private static boolean isMemberTicket(MovieTicketType t) {
    if (TicketTypeSemantics.isMemberRequired(t)) {
      return true;
    }
    if (t.getAudienceCategory() != null || t.getMemberRequired() != null) {
      return false;
    }
    String name = t.getName();
    return name != null && !name.isBlank() && LEGACY_MEMBER_NAME.matcher(name).find();
  }

  private static String promoLabel(MovieTicketType t) {
    if (t.getServiceDayCode() != null && !t.getServiceDayCode().isBlank()) {
      return shortenLabel(t.getServiceDayCode());
    }
    return shortenLabel(t.getName());
  }

  private static String shortenLabel(String name) {
    if (name == null) {
      return null;
    }
    String s = name.trim();
    if (s.length() <= 12) {
      return s;
    }
    return s.substring(0, 12) + "…";
  }
}
