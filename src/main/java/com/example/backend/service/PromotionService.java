package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.CinemaPriceRulesConfig;
import com.example.backend.entity.PricingRule;
import com.example.backend.entity.PromotionMonthlyDay;
import com.example.backend.entity.PromotionSpecificDate;
import com.example.backend.entity.PromotionTimeRange;
import com.example.backend.entity.PromotionWeeklyDay;
import com.example.backend.mapper.CinemaPriceRulesConfigMapper;
import com.example.backend.mapper.PricingRuleMapper;
import com.example.backend.mapper.PromotionMonthlyDayMapper;
import com.example.backend.mapper.PromotionSpecificDateMapper;
import com.example.backend.mapper.PromotionTimeRangeMapper;
import com.example.backend.mapper.PromotionWeeklyDayMapper;
import com.example.backend.query.promotion.PromotionListQuery;
import com.example.backend.query.promotion.PromotionSaveQuery;
import com.example.backend.response.promotion.PromotionDetailResponse;
import com.example.backend.response.promotion.PromotionListItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

  @Autowired
  private CinemaPriceRulesConfigMapper configMapper;

  @Autowired
  private PromotionMonthlyDayMapper promotionMonthlyDayMapper;

  @Autowired
  private PromotionWeeklyDayMapper promotionWeeklyDayMapper;

  @Autowired
  private PromotionSpecificDateMapper promotionSpecificDateMapper;

  @Autowired
  private PromotionTimeRangeMapper promotionTimeRangeMapper;

  @Autowired
  private PricingRuleMapper pricingRuleMapper;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * 按影院获取价格策略（一影院一配置 + 规则），供 app 端按顺序匹配计算价格。
   */
  public List<PromotionListItemResponse> listPromotionsByCinema(Integer cinemaId) {
    if (cinemaId == null) {
      return Collections.emptyList();
    }
    CinemaPriceRulesConfig config = configMapper.selectOne(
      Wrappers.<CinemaPriceRulesConfig>lambdaQuery().eq(CinemaPriceRulesConfig::getCinemaId, cinemaId)
    );
    if (config == null) {
      return Collections.emptyList();
    }
    PromotionListItemResponse dto = buildListItemResponse(config);
    dto.setMonthlyDays(selectMonthlyByCinema(cinemaId));
    dto.setWeeklyDays(selectWeeklyByCinema(cinemaId));
    dto.setSpecificDates(selectSpecificByCinema(cinemaId));
    dto.setTimeRanges(selectTimeRangesByCinema(cinemaId));
    return Collections.singletonList(dto);
  }

  public PromotionDetailResponse getPromotionDetail(Integer cinemaId, Integer promotionId) {
    CinemaPriceRulesConfig config = configMapper.selectOne(
      Wrappers.<CinemaPriceRulesConfig>lambdaQuery().eq(CinemaPriceRulesConfig::getCinemaId, cinemaId)
    );
    if (config == null) {
      PromotionDetailResponse empty = new PromotionDetailResponse();
      empty.setRemark("");
      empty.setAllowMuviticket(false);
      empty.setMonthlyPriority(0);
      empty.setWeeklyPriority(0);
      empty.setSpecificDatePriority(0);
      empty.setTimeRangePriority(0);
      empty.setFixedPricePriority(0);
      empty.setTicketTypePriority(0);
      empty.setPricingRules(Collections.emptyList());
      empty.setMonthlyDays(Collections.emptyList());
      empty.setWeeklyDays(Collections.emptyList());
      empty.setSpecificDates(Collections.emptyList());
      empty.setTimeRanges(Collections.emptyList());
      return empty;
    }
    PromotionDetailResponse response = new PromotionDetailResponse();
    response.setPromotionId(config.getId());
    response.setRemark(config.getRemark());
    response.setAllowMuviticket(Boolean.TRUE.equals(config.getAllowMuviticket()));
    response.setMonthlyPriority(config.getMonthlyPriority() != null ? config.getMonthlyPriority() : 0);
    response.setWeeklyPriority(config.getWeeklyPriority() != null ? config.getWeeklyPriority() : 0);
    response.setSpecificDatePriority(config.getSpecificDatePriority() != null ? config.getSpecificDatePriority() : 0);
    response.setTimeRangePriority(config.getTimeRangePriority() != null ? config.getTimeRangePriority() : 0);
    response.setFixedPricePriority(config.getFixedPricePriority() != null ? config.getFixedPricePriority() : 0);
    response.setTicketTypePriority(config.getTicketTypePriority() != null ? config.getTicketTypePriority() : 0);
    response.setMonthlyDays(selectMonthlyByCinema(cinemaId));
    response.setWeeklyDays(selectWeeklyByCinema(cinemaId));
    response.setSpecificDates(selectSpecificByCinema(cinemaId));
    response.setTimeRanges(selectTimeRangesByCinema(cinemaId));
    List<PricingRule> rules = pricingRuleMapper.selectList(
      Wrappers.<PricingRule>lambdaQuery()
        .eq(PricingRule::getCinemaId, cinemaId)
        .orderByAsc(PricingRule::getPriority)
    );
    response.setPricingRules(rules.stream().map(r -> {
      PromotionDetailResponse.PricingRuleItem dto = new PromotionDetailResponse.PricingRuleItem();
      dto.setId(r.getId());
      dto.setAudienceType(r.getAudienceType());
      dto.setValue(r.getValue());
      dto.setPriority(r.getPriority() != null ? r.getPriority() : 0);
      return dto;
    }).collect(Collectors.toList()));
    return response;
  }

  public IPage<PromotionListItemResponse> listPromotions(PromotionListQuery query) {
    Page<CinemaPriceRulesConfig> page = new Page<>(query.getPage(), query.getPageSize());
    var wrapper = Wrappers.<CinemaPriceRulesConfig>lambdaQuery();
    if (query.getCinemaId() != null) {
      wrapper.eq(CinemaPriceRulesConfig::getCinemaId, query.getCinemaId());
    }
    wrapper.orderByDesc(CinemaPriceRulesConfig::getUpdateTime);
    IPage<CinemaPriceRulesConfig> result = configMapper.selectPage(page, wrapper);
    return result.convert(config -> {
      PromotionListItemResponse dto = buildListItemResponse(config);
      Integer cid = config.getCinemaId();
      dto.setMonthlyDays(selectMonthlyByCinema(cid));
      dto.setWeeklyDays(selectWeeklyByCinema(cid));
      dto.setSpecificDates(selectSpecificByCinema(cid));
      dto.setTimeRanges(selectTimeRangesByCinema(cid));
      return dto;
    });
  }

  @Transactional
  public void savePromotion(PromotionSaveQuery query) {
    Integer cinemaId = query.getCinemaId();
    CinemaPriceRulesConfig config = configMapper.selectOne(
      Wrappers.<CinemaPriceRulesConfig>lambdaQuery().eq(CinemaPriceRulesConfig::getCinemaId, cinemaId)
    );
    boolean isCreate = (config == null);
    if (isCreate) {
      config = new CinemaPriceRulesConfig();
      config.setCinemaId(cinemaId);
      config.setRemark(query.getRemark());
      config.setAllowMuviticket(Boolean.TRUE.equals(query.getAllowMuviticket()));
      config.setMonthlyPriority(query.getMonthlyPriority() != null ? query.getMonthlyPriority() : 0);
      config.setWeeklyPriority(query.getWeeklyPriority() != null ? query.getWeeklyPriority() : 0);
      config.setSpecificDatePriority(query.getSpecificDatePriority() != null ? query.getSpecificDatePriority() : 0);
      config.setTimeRangePriority(query.getTimeRangePriority() != null ? query.getTimeRangePriority() : 0);
      config.setFixedPricePriority(query.getFixedPricePriority() != null ? query.getFixedPricePriority() : 0);
      config.setTicketTypePriority(query.getTicketTypePriority() != null ? query.getTicketTypePriority() : 0);
      configMapper.insert(config);
    } else {
      config.setRemark(query.getRemark());
      config.setAllowMuviticket(Boolean.TRUE.equals(query.getAllowMuviticket()));
      config.setMonthlyPriority(query.getMonthlyPriority() != null ? query.getMonthlyPriority() : 0);
      config.setWeeklyPriority(query.getWeeklyPriority() != null ? query.getWeeklyPriority() : 0);
      config.setSpecificDatePriority(query.getSpecificDatePriority() != null ? query.getSpecificDatePriority() : 0);
      config.setTimeRangePriority(query.getTimeRangePriority() != null ? query.getTimeRangePriority() : 0);
      config.setFixedPricePriority(query.getFixedPricePriority() != null ? query.getFixedPricePriority() : 0);
      config.setTicketTypePriority(query.getTicketTypePriority() != null ? query.getTicketTypePriority() : 0);
      configMapper.updateById(config);

      pricingRuleMapper.delete(Wrappers.<PricingRule>lambdaQuery().eq(PricingRule::getCinemaId, cinemaId));
      promotionMonthlyDayMapper.delete(Wrappers.<PromotionMonthlyDay>lambdaQuery().eq(PromotionMonthlyDay::getCinemaId, cinemaId));
      promotionWeeklyDayMapper.delete(Wrappers.<PromotionWeeklyDay>lambdaQuery().eq(PromotionWeeklyDay::getCinemaId, cinemaId));
      promotionSpecificDateMapper.delete(Wrappers.<PromotionSpecificDate>lambdaQuery().eq(PromotionSpecificDate::getCinemaId, cinemaId));
      promotionTimeRangeMapper.delete(Wrappers.<PromotionTimeRange>lambdaQuery().eq(PromotionTimeRange::getCinemaId, cinemaId));
    }

    if (!CollectionUtils.isEmpty(query.getMonthlyDays())) {
      for (int i = 0; i < query.getMonthlyDays().size(); i++) {
        var item = query.getMonthlyDays().get(i);
        PromotionMonthlyDay entity = new PromotionMonthlyDay();
        entity.setCinemaId(cinemaId);
        entity.setName(item.getName());
        entity.setDayOfMonth(item.getDayOfMonth());
        entity.setPrice(item.getPrice());
        entity.setPriority(item.getPriority() != null ? item.getPriority() : i);
        entity.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        promotionMonthlyDayMapper.insert(entity);
      }
    }
    if (!CollectionUtils.isEmpty(query.getWeeklyDays())) {
      for (int i = 0; i < query.getWeeklyDays().size(); i++) {
        var item = query.getWeeklyDays().get(i);
        PromotionWeeklyDay entity = new PromotionWeeklyDay();
        entity.setCinemaId(cinemaId);
        entity.setName(item.getName());
        entity.setWeekday(item.getWeekday());
        entity.setPrice(item.getPrice());
        entity.setPriority(item.getPriority() != null ? item.getPriority() : i);
        entity.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        promotionWeeklyDayMapper.insert(entity);
      }
    }
    if (!CollectionUtils.isEmpty(query.getSpecificDates())) {
      for (int i = 0; i < query.getSpecificDates().size(); i++) {
        var item = query.getSpecificDates().get(i);
        PromotionSpecificDate entity = new PromotionSpecificDate();
        entity.setCinemaId(cinemaId);
        entity.setName(item.getName());
        entity.setPrice(item.getPrice());
        entity.setPriority(item.getPriority() != null ? item.getPriority() : i);
        entity.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        if (item.getDate() != null) {
          entity.setSpecificDate(Date.valueOf(LocalDate.parse(item.getDate())));
        }
        promotionSpecificDateMapper.insert(entity);
      }
    }
    if (!CollectionUtils.isEmpty(query.getTimeRanges())) {
      for (int i = 0; i < query.getTimeRanges().size(); i++) {
        var item = query.getTimeRanges().get(i);
        PromotionTimeRange entity = new PromotionTimeRange();
        entity.setCinemaId(cinemaId);
        entity.setName(item.getName());
        entity.setApplicableScope(item.getApplicableScope());
        entity.setApplicableDays(item.getApplicableDays());
        entity.setStartTime(item.getStartTime());
        entity.setEndTime(item.getEndTime());
        entity.setPrice(item.getPrice());
        entity.setRemark(item.getRemark());
        entity.setPriority(item.getPriority() != null ? item.getPriority() : i);
        entity.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        promotionTimeRangeMapper.insert(entity);
      }
    }
    if (!CollectionUtils.isEmpty(query.getPricingRules())) {
      for (PromotionSaveQuery.PricingRuleItem item : query.getPricingRules()) {
        if (item.getAudienceType() == null && item.getValue() == null) continue;
        PricingRule rule = new PricingRule();
        rule.setCinemaId(cinemaId);
        rule.setAudienceType(item.getAudienceType() != null ? item.getAudienceType() : 1);
        rule.setValue(item.getValue() != null ? item.getValue() : java.math.BigDecimal.ZERO);
        rule.setPriority(item.getPriority() != null ? item.getPriority() : 0);
        pricingRuleMapper.insert(rule);
      }
    }
  }

  /** 按配置 id 删除：删除该影院配置及其全部规则 */
  @Transactional
  public void deletePromotion(Integer id) {
    if (id == null) throw new IllegalArgumentException("id cannot be null");
    CinemaPriceRulesConfig config = configMapper.selectById(id);
    if (config == null) return;
    Integer cinemaId = config.getCinemaId();
    promotionMonthlyDayMapper.delete(Wrappers.<PromotionMonthlyDay>lambdaQuery().eq(PromotionMonthlyDay::getCinemaId, cinemaId));
    promotionWeeklyDayMapper.delete(Wrappers.<PromotionWeeklyDay>lambdaQuery().eq(PromotionWeeklyDay::getCinemaId, cinemaId));
    promotionSpecificDateMapper.delete(Wrappers.<PromotionSpecificDate>lambdaQuery().eq(PromotionSpecificDate::getCinemaId, cinemaId));
    promotionTimeRangeMapper.delete(Wrappers.<PromotionTimeRange>lambdaQuery().eq(PromotionTimeRange::getCinemaId, cinemaId));
    pricingRuleMapper.delete(Wrappers.<PricingRule>lambdaQuery().eq(PricingRule::getCinemaId, cinemaId));
    configMapper.deleteById(id);
  }

  private PromotionListItemResponse buildListItemResponse(CinemaPriceRulesConfig config) {
    PromotionListItemResponse dto = new PromotionListItemResponse();
    dto.setId(config.getId());
    dto.setCinemaId(config.getCinemaId());
    dto.setRemark(config.getRemark());
    dto.setMonthlyPriority(config.getMonthlyPriority() != null ? config.getMonthlyPriority() : 0);
    dto.setWeeklyPriority(config.getWeeklyPriority() != null ? config.getWeeklyPriority() : 0);
    dto.setSpecificDatePriority(config.getSpecificDatePriority() != null ? config.getSpecificDatePriority() : 0);
    dto.setTimeRangePriority(config.getTimeRangePriority() != null ? config.getTimeRangePriority() : 0);
    dto.setFixedPricePriority(config.getFixedPricePriority() != null ? config.getFixedPricePriority() : 0);
    dto.setTicketTypePriority(config.getTicketTypePriority() != null ? config.getTicketTypePriority() : 0);
    dto.setCreateTime(config.getCreateTime());
    dto.setUpdateTime(config.getUpdateTime());
    return dto;
  }

  private List<PromotionDetailResponse.MonthlyDayItem> selectMonthlyByCinema(Integer cinemaId) {
    return promotionMonthlyDayMapper
      .selectList(Wrappers.<PromotionMonthlyDay>lambdaQuery()
        .eq(PromotionMonthlyDay::getCinemaId, cinemaId)
        .orderByAsc(PromotionMonthlyDay::getPriority)
        .orderByAsc(PromotionMonthlyDay::getDayOfMonth))
      .stream()
      .map(item -> {
        PromotionDetailResponse.MonthlyDayItem dto = new PromotionDetailResponse.MonthlyDayItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDayOfMonth(item.getDayOfMonth());
        dto.setPrice(item.getPrice());
        dto.setPriority(item.getPriority() != null ? item.getPriority() : 0);
        dto.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        return dto;
      })
      .collect(Collectors.toList());
  }

  private List<PromotionDetailResponse.WeeklyDayItem> selectWeeklyByCinema(Integer cinemaId) {
    return promotionWeeklyDayMapper
      .selectList(Wrappers.<PromotionWeeklyDay>lambdaQuery()
        .eq(PromotionWeeklyDay::getCinemaId, cinemaId)
        .orderByAsc(PromotionWeeklyDay::getPriority)
        .orderByAsc(PromotionWeeklyDay::getWeekday))
      .stream()
      .map(item -> {
        PromotionDetailResponse.WeeklyDayItem dto = new PromotionDetailResponse.WeeklyDayItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setWeekday(item.getWeekday());
        dto.setPrice(item.getPrice());
        dto.setPriority(item.getPriority() != null ? item.getPriority() : 0);
        dto.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        return dto;
      })
      .collect(Collectors.toList());
  }

  private List<PromotionDetailResponse.SpecificDateItem> selectSpecificByCinema(Integer cinemaId) {
    return promotionSpecificDateMapper
      .selectList(Wrappers.<PromotionSpecificDate>lambdaQuery()
        .eq(PromotionSpecificDate::getCinemaId, cinemaId)
        .orderByAsc(PromotionSpecificDate::getPriority)
        .orderByAsc(PromotionSpecificDate::getSpecificDate))
      .stream()
      .map(item -> {
        PromotionDetailResponse.SpecificDateItem dto = new PromotionDetailResponse.SpecificDateItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setDate(item.getSpecificDate() == null ? null : DATE_FORMAT.format(item.getSpecificDate()));
        dto.setPriority(item.getPriority() != null ? item.getPriority() : 0);
        dto.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        return dto;
      })
      .collect(Collectors.toList());
  }

  private List<PromotionDetailResponse.TimeRangeItem> selectTimeRangesByCinema(Integer cinemaId) {
    return promotionTimeRangeMapper
      .selectList(Wrappers.<PromotionTimeRange>lambdaQuery()
        .eq(PromotionTimeRange::getCinemaId, cinemaId)
        .orderByAsc(PromotionTimeRange::getPriority)
        .orderByAsc(PromotionTimeRange::getId))
      .stream()
      .map(item -> {
        PromotionDetailResponse.TimeRangeItem dto = new PromotionDetailResponse.TimeRangeItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setApplicableScope(item.getApplicableScope());
        dto.setApplicableDays(item.getApplicableDays());
        dto.setStartTime(item.getStartTime());
        dto.setEndTime(item.getEndTime());
        dto.setPrice(item.getPrice());
        dto.setRemark(item.getRemark());
        dto.setPriority(item.getPriority() != null ? item.getPriority() : 0);
        dto.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
        return dto;
      })
      .collect(Collectors.toList());
  }
}
