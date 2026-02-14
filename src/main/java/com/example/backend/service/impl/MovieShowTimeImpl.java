package com.example.backend.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieShowTimeTicketType;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieShowTimeTicketTypeMapper;
import com.example.backend.query.MovieShowTimeQuery;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class MovieShowTimeImpl  extends ServiceImpl<MovieShowTimeMapper, MovieShowTime>  implements MovieShowTimeService  {

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieShowTimeTicketTypeMapper movieShowTimeTicketTypeMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateScreeningState() {
    QueryWrapper<MovieShowTime> queryWrapper = new QueryWrapper<>();
    queryWrapper.ne("status", ShowTimeState.ended.getCode());
    List<MovieShowTime> data = movieShowTimeMapper.selectList(queryWrapper);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    List<MovieShowTime> toUpdate = data.stream().map(item -> {
      if (item.getStartTime() == null || item.getStartTime().isEmpty()
          || item.getEndTime() == null || item.getEndTime().isEmpty()) {
        log.warn("场次 start_time/end_time 为空，跳过 id={}", item.getId());
        return null;
      }
      try {
        LocalDateTime start = LocalDateTime.parse(item.getStartTime(), formatter);
        LocalDateTime end = LocalDateTime.parse(item.getEndTime(), formatter);
        if (now.isAfter(start) && now.isBefore(end)) {
          item.setStatus(ShowTimeState.screening.getCode());
        } else if (now.isAfter(end)) {
          item.setStatus(ShowTimeState.ended.getCode());
        } else {
          item.setStatus(ShowTimeState.no_started.getCode());
        }
        return item;
      } catch (Exception e) {
        log.warn("场次时间解析失败 id={}, startTime={}, endTime={}", item.getId(), item.getStartTime(), item.getEndTime(), e);
        return null;
      }
    }).filter(item -> item != null).toList();
    if (!toUpdate.isEmpty()) {
      updateBatchById(toUpdate, toUpdate.size());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updatePublishAndCanSaleState() {
    QueryWrapper<MovieShowTime> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("deleted", 0);
    List<MovieShowTime> data = movieShowTimeMapper.selectList(queryWrapper);
    if (data.isEmpty()) {
      return;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    List<MovieShowTime> toUpdate = data.stream().map(item -> {
      boolean changed = false;
      if (item.getPublishAt() != null && !item.getPublishAt().isEmpty()) {
        try {
          LocalDateTime publishAt = LocalDateTime.parse(item.getPublishAt(), formatter);
          boolean shouldOpen = !now.isBefore(publishAt);
          Boolean currentOpen = item.getOpen();
          if (shouldOpen && (currentOpen == null || !currentOpen)) {
            item.setOpen(true);
            changed = true;
          }
        } catch (Exception e) {
          log.warn("publish_at 解析失败, id={}, value={}", item.getId(), item.getPublishAt(), e);
        }
      }
      Boolean originalCanSale = item.getCanSale();
      Boolean newCanSale = originalCanSale;
      String saleOpenAt = item.getSaleOpenAt();
      if (saleOpenAt == null || saleOpenAt.isEmpty()) {
        newCanSale = Boolean.TRUE;
      } else {
        try {
          LocalDateTime saleOpenTime = LocalDateTime.parse(saleOpenAt, formatter);
          newCanSale = !now.isBefore(saleOpenTime);
        } catch (Exception e) {
          log.warn("sale_open_at 解析失败, id={}, value={}", item.getId(), saleOpenAt, e);
        }
      }
      if (newCanSale != null && (originalCanSale == null || !newCanSale.equals(originalCanSale))) {
        item.setCanSale(newCanSale);
        changed = true;
      }
      return changed ? item : null;
    }).filter(item -> item != null).toList();
    if (!toUpdate.isEmpty()) {
      updateBatchById(toUpdate, toUpdate.size());
    }
  }

  public List<MovieShowTime> getSortedMovieShowTimes(MovieShowTimeQuery query, String format) {
    QueryWrapper<MovieShowTime> wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());
    wrapper.eq("theater_hall_id", query.getTheaterHallId());

    // 如果是编辑的时候，则不考虑当前区间
    if (query.getId() != null) {
      wrapper.ne("id", query.getId());
    }

    List<MovieShowTime> list = movieShowTimeMapper.selectList(wrapper);
    list.sort((o1, o2) -> {
      try {
        Date o1StartTimestamp = Utils.getTimestamp(o1.getStartTime(), format);
        Date o2StartTimestamp = Utils.getTimestamp(o2.getStartTime(), format);
        return Long.compare(o1StartTimestamp.getTime(), o2StartTimestamp.getTime());
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    });
    return list;
  }

  public void saveMovieShowTimeIfNotExists(MovieShowTimeQuery query, String format) throws ParseException {
    MovieShowTime movieShowTime = new MovieShowTime();

    movieShowTime.setCinemaId(query.getCinemaId());
    movieShowTime.setTheaterHallId(query.getTheaterHallId());
    movieShowTime.setMovieId(query.getMovieId());
    movieShowTime.setStartTime(query.getStartTime());
    movieShowTime.setEndTime(query.getEndTime());
    movieShowTime.setOpen(query.getOpen() != null ? query.getOpen() : true);
    movieShowTime.setSpecIds(query.getSpecIds() != null ? query.getSpecIds() : new ArrayList<>());
    movieShowTime.setDimensionType(query.getDimensionType());
    movieShowTime.setSubtitleId(query.getSubtitleId());
    movieShowTime.setShowTimeTagId(query.getShowTimeTagId());
    movieShowTime.setMovieVersionId(query.getMovieVersionId());
    movieShowTime.setPricingMode(query.getPricingMode());
    movieShowTime.setFixedAmount(query.getFixedAmount());
    movieShowTime.setSurcharge(query.getSurcharge());
    movieShowTime.setAllowPresale(query.getAllowPresale() != null ? query.getAllowPresale() : false);
    movieShowTime.setPublishAt(query.getPublishAt());
    movieShowTime.setSaleOpenAt(query.getSaleOpenAt());

    if (query.getShowTimeTagId() != null) {
//      movieShowTime.setShowTimeTagId( query.getShowTimeTagId());
    }
    if (query.getSubtitleId() != null) {
//      movieShowTime.setSubtitleId(query.getSubtitleId());
    }

    if (query.getId() == null) {
      movieShowTimeMapper.insert(movieShowTime);
      log.debug("场次插入成功");
    } else {
      movieShowTime.setId(query.getId());
      movieShowTimeMapper.updateById(movieShowTime);
      log.debug("场次更新成功");
    }

    Integer showTimeId = movieShowTime.getId();
    saveShowTimeTicketTypeConfig(showTimeId, query);
  }

  /** 场次限定票种规则写入单独表（默认规则时） */
  private void saveShowTimeTicketTypeConfig(Integer showTimeId, MovieShowTimeQuery query) {
    movieShowTimeTicketTypeMapper.deleteByShowTimeId(showTimeId);

    Map<Integer, BigDecimal> overrides = query.getTicketTypeOverrides();
    Map<Integer, Boolean> enabledMap = query.getTicketTypeEnabled();
    if ((overrides == null || overrides.isEmpty()) && (enabledMap == null || enabledMap.isEmpty())) {
      return;
    }

    Set<Integer> ticketTypeIds = new HashSet<>();
    if (overrides != null) ticketTypeIds.addAll(overrides.keySet());
    if (enabledMap != null) ticketTypeIds.addAll(enabledMap.keySet());

    for (Integer ticketTypeId : ticketTypeIds) {
      BigDecimal overridePrice = overrides != null ? overrides.get(ticketTypeId) : null;
      Boolean enabled = enabledMap == null || !enabledMap.containsKey(ticketTypeId) ? Boolean.TRUE : enabledMap.get(ticketTypeId);
      MovieShowTimeTicketType row = new MovieShowTimeTicketType();
      row.setShowTimeId(showTimeId);
      row.setTicketTypeId(ticketTypeId);
      row.setOverridePrice(overridePrice);
      row.setEnabled(enabled != null ? enabled : true);
      movieShowTimeTicketTypeMapper.insert(row);
    }
  }

  public boolean check(List<MovieShowTime> list, String format, MovieShowTimeQuery query) {
    try {
      // 获取传入的开始和结束时间
      Date queryStartTime = Utils.getTimestamp(query.getStartTime(), format);
      Date queryEndTime = Utils.getTimestamp(query.getEndTime(), format);

      // 遍历已有的时间区间，检查是否有重叠
      for (int i = 0; i < list.size(); i++) {
        MovieShowTime item = list.get(i);
        Date startTime = Utils.getTimestamp(item.getStartTime(), format);
        Date endTime = Utils.getTimestamp(item.getEndTime(), format);

        // 边界检查，避免访问越界
        Date nextStartTime = (i + 1 < list.size()) ? Utils.getTimestamp(list.get(i + 1).getStartTime(), format) : null;

        if (nextStartTime != null) {
          log.debug("下个时间为: {}", Utils.format(nextStartTime, format));
        }

        // 检查当前时间区间与传入时间区间是否有重叠
        if (!endTime.before(queryStartTime) && !startTime.after(queryEndTime)) {
          return false; // 时间区间有重叠，返回false
        }

        // 如果当前时间区间的结束时间在传入时间区间的开始时间之前，且
        // 传入时间区间的结束时间在下一个时间区间的开始时间之前，则可以插入
        if (nextStartTime == null || (!endTime.after(queryStartTime) && !nextStartTime.before(queryEndTime))) {
          saveMovieShowTimeIfNotExists(query, format);
          return true;
        }
      }

      // 如果待插入的时间区间的开始时间在所有时间区间的结束时间之后，可以直接插入到最后一个时间区间之后
      saveMovieShowTimeIfNotExists(query, format);
      return true;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

//  public  boolean check (List<MovieShowTime> list, String format, MovieShowTimeQuery query) {
//    try {
//      // 遍历已有的时间区间，检查是否有重叠
//      for (int i = 0; i < list.size(); i++) {
//        MovieShowTime item = list.get(i);
//        Date queryStartTime = Utils.getTimestamp(query.getStartTime(), format);
//        Date queryEndTime = Utils.getTimestamp(query.getEndTime(), format);
//        Date startTime = Utils.getTimestamp(item.getStartTime(), format);
//        Date endTime = Utils.getTimestamp(item.getEndTime(), format);
//        if (list.get(i + 1) != null) {
//          Date nextStartTime = Utils.getTimestamp(list.get(i + 1).getStartTime(), format);
//          System.out.println("i =======" + i);
//          System.out.println("传参区间为: " + query.getStartTime() + "===" + query.getEndTime());
//          System.out.println("遍历区间为: " + Utils.format(startTime, format) + "===" + Utils.format(endTime, format));
//          System.out.println("下个时间为：" + Utils.format(nextStartTime, format));
//
//          Boolean has = !endTime.after(queryStartTime) && (i == list.size() - 1 || !nextStartTime.before(queryEndTime));
//          // 如果新时间区间的开始时间在当前时间区间的结束时间之后，且新时间区间的结束时间在下一个时间区间的开始时间之前，
//          // 则可以将新时间区间插入到当前时间区间之后
//          if (has) {
//            saveMovieShowTimeIfNotExists(query, format);
//            return true;
//          }
//          // 如果新时间小于数据任意时间
//          if (!endTime.before(queryEndTime) && !queryEndTime.before(queryStartTime)) {
//            saveMovieShowTimeIfNotExists(query, format);
//            return true;
//          }
//          // 如果新时间区间与当前时间区间有重叠，则直接返回时间冲突的错误信息
//          if (!endTime.before(queryStartTime) && !startTime.after(queryEndTime)) {
//            return false;
//          }
//        }
//
//      }
//      // 如果待插入的时间区间的开始时间在所有时间区间的结束时间之后，可以直接插入到最后一个时间区间之后
//      saveMovieShowTimeIfNotExists(query, format);
//      return  true;
//    } catch (ParseException e) {
//      throw new RuntimeException(e);
//    }
//  }
}