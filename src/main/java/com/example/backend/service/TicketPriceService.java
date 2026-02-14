package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.PricingRule;
import com.example.backend.entity.CinemaPriceRulesConfig;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.CinemaPriceRulesConfigMapper;
import com.example.backend.mapper.CinemaSpecSpecMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.mapper.PricingRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 票价计算服务
 * 最终总价 = 基础票价(Base) + 规格补价(Surcharge)
 * 支持：系统活动模式(规则匹配)、固定价格模式、前售券(基础价=0)、固定金额抵扣券
 */
@Service
@RequiredArgsConstructor
public class TicketPriceService {

    private final MovieTicketTypeMapper movieTicketTypeMapper;
    private final CinemaPriceConfigMapper cinemaPriceConfigMapper;
    private final CinemaSpecSpecMapper cinemaSpecSpecMapper;
    private final MovieShowTimeMapper movieShowTimeMapper;
    private final CinemaPriceRulesConfigMapper configMapper;
    private final PricingRuleMapper pricingRuleMapper;

    /** 定价模式：系统活动模式(按规则匹配) */
    public static final int PRICING_MODE_ACTIVITY = 1;
    /** 定价模式：固定价格模式 */
    public static final int PRICING_MODE_FIXED = 2;

    /**
     * 计算票价（原有逻辑：票种 + 3D + 规格）
     */
    public BigDecimal calculatePrice(Integer cinemaId, Integer movieTicketTypeId,
                                     Integer dimensionType, List<Integer> specIds) {
        BigDecimal base = getTicketTypeBasePrice(movieTicketTypeId);
        if (base == null) return BigDecimal.ZERO;
        BigDecimal surcharge = computeSurcharge(cinemaId, dimensionType, specIds);
        return base.add(surcharge);
    }

    /**
     * 按场次与用户身份计算最终票价（支持活动规则、固定价、前售券、金额券）
     *
     * @param showtimeId              场次ID
     * @param audienceType            购票人身份(人群) dict_item.code，用于规则匹配
     * @param movieTicketTypeIdFallback 无活动/规则时的票种ID
     * @param useMuviticket           是否使用前售券(100%抵扣，基础价=0)
     * @param couponAmount            固定金额抵扣券面值，null 表示不使用
     * @return 最终总价 = 基础价(扣券后) + 规格补价
     */
    public BigDecimal calculatePriceByShowtime(Integer showtimeId, Integer audienceType,
                                               Integer movieTicketTypeIdFallback, boolean useMuviticket,
                                               BigDecimal couponAmount) {
        MovieShowTime showtime = showtimeId != null ? movieShowTimeMapper.selectById(showtimeId) : null;
        if (showtime == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal surcharge = getSurchargeForShowtime(showtime);
        BigDecimal base = getBasePriceForShowtime(showtime, audienceType, movieTicketTypeIdFallback, useMuviticket);
        if (base == null) base = BigDecimal.ZERO;
        if (couponAmount != null && couponAmount.compareTo(BigDecimal.ZERO) > 0) {
            base = base.subtract(couponAmount).max(BigDecimal.ZERO);
        }
        return base.add(surcharge);
    }

    /**
     * 场次规格补价：优先用场次配置的 surcharge，否则按 3D+规格计算
     */
    public BigDecimal getSurchargeForShowtime(MovieShowTime showtime) {
        if (showtime.getSurcharge() != null && showtime.getSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            return showtime.getSurcharge();
        }
        return computeSurcharge(showtime.getCinemaId(), showtime.getDimensionType(), showtime.getSpecIds());
    }

  /**
   * 场次基础价：前售券且影院配置允许则 0；固定价模式取 fixed_amount；活动模式按规则匹配；否则票种价
   */
  public BigDecimal getBasePriceForShowtime(MovieShowTime showtime, Integer audienceType,
                                              Integer movieTicketTypeIdFallback, boolean useMuviticket) {
    Integer mode = showtime.getPricingMode() != null ? showtime.getPricingMode() : PRICING_MODE_ACTIVITY;
    Integer cinemaId = showtime.getCinemaId();

    if (useMuviticket && cinemaId != null) {
      CinemaPriceRulesConfig config = configMapper.selectOne(
        new QueryWrapper<CinemaPriceRulesConfig>()
          .eq("cinema_id", cinemaId)
          .last("LIMIT 1")
      );
      if (config != null && Boolean.TRUE.equals(config.getAllowMuviticket())) {
        return BigDecimal.ZERO;
      }
    }

    if (mode == PRICING_MODE_FIXED && showtime.getFixedAmount() != null) {
      return showtime.getFixedAmount();
    }

    if (mode == PRICING_MODE_ACTIVITY && cinemaId != null && audienceType != null) {
      List<PricingRule> rules = pricingRuleMapper.selectList(
        new QueryWrapper<PricingRule>()
          .eq("cinema_id", cinemaId)
          .eq("audience_type", audienceType)
          .orderByAsc("priority")
          .last("LIMIT 1")
      );
      if (!rules.isEmpty() && rules.get(0).getValue() != null) {
        return rules.get(0).getValue();
      }
    }

    return getTicketTypeBasePrice(movieTicketTypeIdFallback);
  }

    private BigDecimal getTicketTypeBasePrice(Integer movieTicketTypeId) {
        if (movieTicketTypeId == null) return BigDecimal.ZERO;
        MovieTicketType ticketType = movieTicketTypeMapper.selectById(movieTicketTypeId);
        return (ticketType != null && ticketType.getPrice() != null) ? ticketType.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal computeSurcharge(Integer cinemaId, Integer dimensionType, List<Integer> specIds) {
        BigDecimal total = BigDecimal.ZERO;
        if (dimensionType != null && cinemaId != null) {
            BigDecimal s = cinemaPriceConfigMapper.getSurcharge(cinemaId, dimensionType);
            if (s != null) total = total.add(s);
        }
        if (specIds != null && cinemaId != null) {
            for (Integer specId : specIds) {
                if (specId == null) continue;
                QueryWrapper<CinemaSpecSpec> qw = new QueryWrapper<>();
                qw.eq("cinema_id", cinemaId).eq("spec_id", specId).last("LIMIT 1");
                CinemaSpecSpec specSpec = cinemaSpecSpecMapper.selectOne(qw);
                if (specSpec != null && specSpec.getPlusPrice() != null) {
                    total = total.add(BigDecimal.valueOf(specSpec.getPlusPrice()));
                }
            }
        }
        return total;
    }
}
