package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Promotion;
import com.example.backend.entity.PromotionMonthlyDay;
import com.example.backend.entity.PromotionSpecificDate;
import com.example.backend.entity.PromotionTimeRange;
import com.example.backend.entity.PromotionWeeklyDay;
import com.example.backend.mapper.PromotionMapper;
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
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromotionService {

  @Autowired
  private PromotionMapper promotionMapper;

  @Autowired
  private PromotionMonthlyDayMapper promotionMonthlyDayMapper;

  @Autowired
  private PromotionWeeklyDayMapper promotionWeeklyDayMapper;

  @Autowired
  private PromotionSpecificDateMapper promotionSpecificDateMapper;

  @Autowired
  private PromotionTimeRangeMapper promotionTimeRangeMapper;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public PromotionDetailResponse getPromotionDetail(Integer cinemaId) {
    Promotion promotion = promotionMapper.selectOne(
      Wrappers.<Promotion>lambdaQuery()
        .eq(Promotion::getCinemaId, cinemaId)
        .last("LIMIT 1")
    );

    if (promotion == null) {
      PromotionDetailResponse empty = new PromotionDetailResponse();
      empty.setName("");
      empty.setRemark("");
      empty.setMonthlyDays(Collections.emptyList());
      empty.setWeeklyDays(Collections.emptyList());
      empty.setSpecificDates(Collections.emptyList());
      empty.setTimeRanges(Collections.emptyList());
      return empty;
    }

    PromotionDetailResponse response = new PromotionDetailResponse();
    response.setPromotionId(promotion.getId());
    response.setName(promotion.getName());
    response.setRemark(promotion.getRemark());

    response.setMonthlyDays(
      promotionMonthlyDayMapper
        .selectList(Wrappers.<PromotionMonthlyDay>lambdaQuery()
          .eq(PromotionMonthlyDay::getPromotionId, promotion.getId())
          .orderByAsc(PromotionMonthlyDay::getDayOfMonth))
        .stream()
        .map(item -> {
          PromotionDetailResponse.MonthlyDayItem dto = new PromotionDetailResponse.MonthlyDayItem();
          dto.setId(item.getId());
          dto.setName(item.getName());
          dto.setDayOfMonth(item.getDayOfMonth());
          dto.setPrice(item.getPrice());
          return dto;
        })
        .collect(Collectors.toList())
    );

    response.setWeeklyDays(
      promotionWeeklyDayMapper
        .selectList(Wrappers.<PromotionWeeklyDay>lambdaQuery()
          .eq(PromotionWeeklyDay::getPromotionId, promotion.getId())
          .orderByAsc(PromotionWeeklyDay::getWeekday))
        .stream()
        .map(item -> {
          PromotionDetailResponse.WeeklyDayItem dto = new PromotionDetailResponse.WeeklyDayItem();
          dto.setId(item.getId());
          dto.setName(item.getName());
          dto.setWeekday(item.getWeekday());
          dto.setPrice(item.getPrice());
          return dto;
        })
        .collect(Collectors.toList())
    );

    response.setSpecificDates(
      promotionSpecificDateMapper
        .selectList(Wrappers.<PromotionSpecificDate>lambdaQuery()
          .eq(PromotionSpecificDate::getPromotionId, promotion.getId())
          .orderByAsc(PromotionSpecificDate::getSpecificDate))
        .stream()
        .map(item -> {
          PromotionDetailResponse.SpecificDateItem dto = new PromotionDetailResponse.SpecificDateItem();
          dto.setId(item.getId());
          dto.setName(item.getName());
          dto.setPrice(item.getPrice());
          dto.setDate(item.getSpecificDate() == null ? null : DATE_FORMAT.format(item.getSpecificDate()));
          return dto;
        })
        .collect(Collectors.toList())
    );

    response.setTimeRanges(
      promotionTimeRangeMapper
        .selectList(Wrappers.<PromotionTimeRange>lambdaQuery()
          .eq(PromotionTimeRange::getPromotionId, promotion.getId())
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
          return dto;
        })
        .collect(Collectors.toList())
    );

    return response;
  }

  public IPage<PromotionListItemResponse> listPromotions(PromotionListQuery query) {
    Page<Promotion> page = new Page<>(query.getPage(), query.getPageSize());
    var wrapper = Wrappers.<Promotion>lambdaQuery();
    if (query.getCinemaId() != null) {
      wrapper.eq(Promotion::getCinemaId, query.getCinemaId());
    }
    if (StringUtils.hasText(query.getName())) {
      wrapper.like(Promotion::getName, query.getName());
    }
    wrapper.orderByDesc(Promotion::getUpdateTime);

    IPage<Promotion> result = promotionMapper.selectPage(page, wrapper);

    List<Integer> promotionIds = result.getRecords().stream().map(Promotion::getId).toList();

    Map<Integer, List<PromotionDetailResponse.MonthlyDayItem>> monthlyMap = new HashMap<>();
    Map<Integer, List<PromotionDetailResponse.WeeklyDayItem>> weeklyMap = new HashMap<>();
    Map<Integer, List<PromotionDetailResponse.SpecificDateItem>> dateMap = new HashMap<>();
    Map<Integer, List<PromotionDetailResponse.TimeRangeItem>> timeMap = new HashMap<>();

    if (!promotionIds.isEmpty()) {
      promotionMonthlyDayMapper.selectList(
          Wrappers.<PromotionMonthlyDay>lambdaQuery().in(PromotionMonthlyDay::getPromotionId, promotionIds)
      ).forEach(item -> {
        PromotionDetailResponse.MonthlyDayItem dto = new PromotionDetailResponse.MonthlyDayItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDayOfMonth(item.getDayOfMonth());
        dto.setPrice(item.getPrice());
        monthlyMap.computeIfAbsent(item.getPromotionId(), k -> new ArrayList<>()).add(dto);
      });

      promotionWeeklyDayMapper.selectList(
          Wrappers.<PromotionWeeklyDay>lambdaQuery().in(PromotionWeeklyDay::getPromotionId, promotionIds)
      ).forEach(item -> {
        PromotionDetailResponse.WeeklyDayItem dto = new PromotionDetailResponse.WeeklyDayItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setWeekday(item.getWeekday());
        dto.setPrice(item.getPrice());
        weeklyMap.computeIfAbsent(item.getPromotionId(), k -> new ArrayList<>()).add(dto);
      });

      promotionSpecificDateMapper.selectList(
          Wrappers.<PromotionSpecificDate>lambdaQuery().in(PromotionSpecificDate::getPromotionId, promotionIds)
      ).forEach(item -> {
        PromotionDetailResponse.SpecificDateItem dto = new PromotionDetailResponse.SpecificDateItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setDate(item.getSpecificDate() == null ? null : DATE_FORMAT.format(item.getSpecificDate()));
        dateMap.computeIfAbsent(item.getPromotionId(), k -> new ArrayList<>()).add(dto);
      });

      promotionTimeRangeMapper.selectList(
          Wrappers.<PromotionTimeRange>lambdaQuery().in(PromotionTimeRange::getPromotionId, promotionIds)
      ).forEach(item -> {
        PromotionDetailResponse.TimeRangeItem dto = new PromotionDetailResponse.TimeRangeItem();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setApplicableScope(item.getApplicableScope());
        dto.setApplicableDays(item.getApplicableDays());
        dto.setStartTime(item.getStartTime());
        dto.setEndTime(item.getEndTime());
        dto.setPrice(item.getPrice());
        dto.setRemark(item.getRemark());
        timeMap.computeIfAbsent(item.getPromotionId(), k -> new ArrayList<>()).add(dto);
      });
    }

    return result.convert(item -> {
      PromotionListItemResponse dto = new PromotionListItemResponse();
      dto.setId(item.getId());
      dto.setCinemaId(item.getCinemaId());
      dto.setName(item.getName());
      dto.setRemark(item.getRemark());
      dto.setCreateTime(item.getCreateTime());
      dto.setUpdateTime(item.getUpdateTime());
      dto.setMonthlyDays(monthlyMap.getOrDefault(item.getId(), Collections.emptyList()));
      dto.setWeeklyDays(weeklyMap.getOrDefault(item.getId(), Collections.emptyList()));
      dto.setSpecificDates(dateMap.getOrDefault(item.getId(), Collections.emptyList()));
      dto.setTimeRanges(timeMap.getOrDefault(item.getId(), Collections.emptyList()));
      return dto;
    });
  }

  @Transactional
  public void savePromotion(PromotionSaveQuery query) {
    Promotion promotion;
    boolean isCreate = (query.getId() == null);

    if (isCreate) {
      promotion = new Promotion();
      promotion.setCinemaId(query.getCinemaId());
      promotion.setName(query.getName());
      promotion.setRemark(query.getRemark());
      promotionMapper.insert(promotion);
    } else {
      promotion = promotionMapper.selectById(query.getId());
      if (promotion == null) {
        throw new IllegalArgumentException("Promotion not found");
      }
      promotion.setName(query.getName());
      promotion.setRemark(query.getRemark());
      promotionMapper.updateById(promotion);

      promotionMonthlyDayMapper.delete(
        Wrappers.<PromotionMonthlyDay>lambdaQuery().eq(PromotionMonthlyDay::getPromotionId, promotion.getId())
      );
      promotionWeeklyDayMapper.delete(
        Wrappers.<PromotionWeeklyDay>lambdaQuery().eq(PromotionWeeklyDay::getPromotionId, promotion.getId())
      );
      promotionSpecificDateMapper.delete(
        Wrappers.<PromotionSpecificDate>lambdaQuery().eq(PromotionSpecificDate::getPromotionId, promotion.getId())
      );
      promotionTimeRangeMapper.delete(
        Wrappers.<PromotionTimeRange>lambdaQuery().eq(PromotionTimeRange::getPromotionId, promotion.getId())
      );
    }

    Integer promotionId = promotion.getId();

    if (!CollectionUtils.isEmpty(query.getMonthlyDays())) {
      List<PromotionMonthlyDay> batch = query.getMonthlyDays().stream().map(item -> {
        PromotionMonthlyDay entity = new PromotionMonthlyDay();
        entity.setPromotionId(promotionId);
        entity.setName(item.getName());
        entity.setDayOfMonth(item.getDayOfMonth());
        entity.setPrice(item.getPrice());
        return entity;
      }).collect(Collectors.toList());
      batch.forEach(promotionMonthlyDayMapper::insert);
    }

    if (!CollectionUtils.isEmpty(query.getWeeklyDays())) {
      List<PromotionWeeklyDay> batch = query.getWeeklyDays().stream().map(item -> {
        PromotionWeeklyDay entity = new PromotionWeeklyDay();
        entity.setPromotionId(promotionId);
        entity.setName(item.getName());
        entity.setWeekday(item.getWeekday());
        entity.setPrice(item.getPrice());
        return entity;
      }).collect(Collectors.toList());
      batch.forEach(promotionWeeklyDayMapper::insert);
    }

    if (!CollectionUtils.isEmpty(query.getSpecificDates())) {
      List<PromotionSpecificDate> batch = query.getSpecificDates().stream().map(item -> {
        PromotionSpecificDate entity = new PromotionSpecificDate();
        entity.setPromotionId(promotionId);
        entity.setName(item.getName());
        entity.setPrice(item.getPrice());
        if (item.getDate() != null) {
          LocalDate localDate = LocalDate.parse(item.getDate());
          entity.setSpecificDate(Date.valueOf(localDate));
        }
        return entity;
      }).collect(Collectors.toList());
      batch.forEach(promotionSpecificDateMapper::insert);
    }

    if (!CollectionUtils.isEmpty(query.getTimeRanges())) {
      List<PromotionTimeRange> batch = query.getTimeRanges().stream().map(item -> {
        PromotionTimeRange entity = new PromotionTimeRange();
        entity.setPromotionId(promotionId);
        entity.setName(item.getName());
        entity.setApplicableScope(item.getApplicableScope());
        entity.setApplicableDays(item.getApplicableDays());
        entity.setStartTime(item.getStartTime());
        entity.setEndTime(item.getEndTime());
        entity.setPrice(item.getPrice());
        entity.setRemark(item.getRemark());
        return entity;
      }).collect(Collectors.toList());
      batch.forEach(promotionTimeRangeMapper::insert);
    }
  }

  @Transactional
  public void deletePromotion(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("promotionId cannot be null");
    }

    promotionMonthlyDayMapper.delete(
      Wrappers.<PromotionMonthlyDay>lambdaQuery().eq(PromotionMonthlyDay::getPromotionId, id)
    );
    promotionWeeklyDayMapper.delete(
      Wrappers.<PromotionWeeklyDay>lambdaQuery().eq(PromotionWeeklyDay::getPromotionId, id)
    );
    promotionSpecificDateMapper.delete(
      Wrappers.<PromotionSpecificDate>lambdaQuery().eq(PromotionSpecificDate::getPromotionId, id)
    );
    promotionTimeRangeMapper.delete(
      Wrappers.<PromotionTimeRange>lambdaQuery().eq(PromotionTimeRange::getPromotionId, id)
    );

    promotionMapper.deleteById(id);
  }
}
