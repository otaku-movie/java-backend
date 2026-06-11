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

  /**
   * movie_show_time 写入互斥用的事务级咨询锁 key；爬虫导入持锁期间这两个每分钟任务跳过本轮，
   * 杜绝两边以不同行序写同一批场次而成环死锁。**必须与爬虫端 SHOWTIME_REFRESH_LOCK_KEY 一致**
   * （cinema-crawler/scripts/pipeline/import-data-pg.ts）。
   */
  private static final long SHOWTIME_REFRESH_LOCK_KEY = 480037L;

  private static final DateTimeFormatter SHOWTIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateScreeningState() {
    // 与爬虫导入用同一把咨询锁串行化：导入正在写 movie_show_time 时直接跳过本轮，
    // 下一分钟自然补算，避免行锁乱序成环死锁。
    if (Boolean.FALSE.equals(movieShowTimeMapper.tryAdvisoryXactLock(SHOWTIME_REFRESH_LOCK_KEY))) {
      log.info("updateScreeningState: 未取得咨询锁（导入进行中），跳过本轮");
      return;
    }
    // 集合式重算：只更新「当前状态 != 应有状态」的行（稳态下每分钟仅几条跨越边界），
    // 不再每分钟把全表载入 JVM。start_time/end_time 为定长文本，按字典序与 now 比较等价于时间比较。
    String now = LocalDateTime.now().format(SHOWTIME_FORMATTER);
    int screening = movieShowTimeMapper.markScreening(ShowTimeState.screening.getCode(), now);
    int ended = movieShowTimeMapper.markEnded(ShowTimeState.ended.getCode(), now);
    int notStarted = movieShowTimeMapper.markNotStarted(ShowTimeState.no_started.getCode(), now);
    if (screening + ended + notStarted > 0) {
      log.info("场次放映状态刷新: 上映中={}, 已结束={}, 未开始={}", screening, ended, notStarted);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updatePublishAndCanSaleState() {
    if (Boolean.FALSE.equals(movieShowTimeMapper.tryAdvisoryXactLock(SHOWTIME_REFRESH_LOCK_KEY))) {
      log.info("updatePublishAndCanSaleState: 未取得咨询锁（导入进行中），跳过本轮");
      return;
    }
    String now = LocalDateTime.now().format(SHOWTIME_FORMATTER);
    int opened = movieShowTimeMapper.openByPublishAt(now);
    int saleNoGate = movieShowTimeMapper.enableSaleWhenNoSaleOpenAt();
    int saleOn = movieShowTimeMapper.enableSaleWhenSaleOpenAtReached(now);
    int saleOff = movieShowTimeMapper.disableSaleWhenSaleOpenAtNotReached(now);
    if (opened + saleNoGate + saleOn + saleOff > 0) {
      log.info("场次公开/可售刷新: 公开+{}, 可售(无门槛)+{}, 可售(到点)+{}, 不可售(未到)+{}",
          opened, saleNoGate, saleOn, saleOff);
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
    movieShowTime.setReReleaseId(query.getReReleaseId());
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