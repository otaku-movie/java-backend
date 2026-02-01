package com.example.backend.controller.scheduledTasks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.config.Config;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.enumerate.MovieReleaseState;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.SeatState;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.service.MovieOrderService;
import com.example.backend.service.MovieService;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.service.impl.MovieShowTimeImpl;
import com.example.backend.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EnableScheduling
@Slf4j
@RestController
public class ScheduledTasksController {

  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private MovieService movieService;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieShowTimeImpl movieShowTimeImpl;

  @Autowired
  private MovieOrderMapper movieOrderMapper;

  @Autowired
  private MovieOrderService movieOrderService;

  @Autowired
  private SelectSeatMapper selectSeatMapper;





  // 更新电影上映日期状态 每天
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_STATE)
  @Transactional
  @Scheduled(cron = "0 0 0 * * ?")  // 每天的 00:00 执行
  public RestBean<Object> updateMovieState() {
    log.info("Updating movie state...");
    QueryWrapper queryWrapper = new QueryWrapper();
    //    过滤掉已结束的
    queryWrapper.ne("status", MovieReleaseState.ended.getType());
    List<Movie> movieList = movieMapper.selectList(queryWrapper);

    List<Movie> result = movieList.stream().filter(item -> {
      String regex = "^\\d{4}-\\d{2}-\\d{2}$";
      if (item.getStartDate() != null) {
        return Pattern.matches(regex, item.getStartDate());
      } else if (item.getEndDate() != null) {
        return Pattern.matches(regex, item.getEndDate());
      } else {
        return false;
      }
    }).map(item -> {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
          // 如果 startDate 和 endDate 都存在
          if (item.getStartDate() != null && item.getEndDate() != null) {
            LocalDate startDate = LocalDate.parse(item.getStartDate(), formatter);
            LocalDate endDate = LocalDate.parse(item.getEndDate(), formatter);

            if (!currentDate.isBefore(startDate) && currentDate.isBefore(endDate)) {
              // 设置为上映中
              item.setStatus(MovieReleaseState.nowShowing.getType());
            } else if (currentDate.isAfter(endDate)) {
              // 设置为已结束
              item.setStatus(MovieReleaseState.ended.getType());
            }
          }
          // 如果只有 startDate
          else if (item.getStartDate() != null) {
            LocalDate startDate = LocalDate.parse(item.getStartDate(), formatter);

            if (currentDate.isAfter(startDate)) {
              // 设置为上映中
              item.setStatus(MovieReleaseState.nowShowing.getType());
            }
          }
        } catch (Exception e) {
          log.warn("日期解析错误: {}", e.getMessage());
        }

        return item;
      })
      .toList();

    if (result.size() > 0) {
      movieService.updateBatchById(result, result.size());
    }
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Success.GENERAL));
  }

  // 更新电影放映状态 每分钟
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_SCREENING_STATE)
  @Transactional
  @Scheduled(cron = "0 * * * * ?")  // 每分钟执行
  public RestBean<Object> updateMovieScreeningState() {
    log.info("Updating movie screening state...");
    QueryWrapper queryWrapper = new QueryWrapper();
//    过滤掉已结束的
    queryWrapper.ne("status", ShowTimeState.ended.getCode());
    List<MovieShowTime> data = movieShowTimeMapper.selectList(queryWrapper);
    List<MovieShowTime> result = data.stream().map(item -> {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      LocalDateTime currentDateTime = LocalDateTime.now(); // 当前时间

      // 解析放映时间
      LocalDateTime startDate = LocalDateTime.parse(item.getStartTime(), formatter);
      LocalDateTime endDate = LocalDateTime.parse(item.getEndTime(), formatter);

      // 根据时间设置状态
      if (currentDateTime.isAfter(startDate) && currentDateTime.isBefore(endDate)) {
        // 设置为上映中
        item.setStatus(ShowTimeState.screening.getCode());
      } else if (currentDateTime.isAfter(endDate)) {
        // 设置为已结束
        item.setStatus(ShowTimeState.ended.getCode());
      } else {
        // 设置为未上映
        item.setStatus(ShowTimeState.no_started.getCode());
      }
      return item;
    }).toList();

    if (result.size() > 0) {
      movieShowTimeImpl.updateBatchById(result, result.size());
    }

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Success.GENERAL));
  }
  // 更新选座状态 超时释放座位
  @Scheduled(cron = "0 * * * * ?")  // 每分钟执行
  public void updateMovieSeatSelectionState() {
    log.info("Updating movie seat selection state...");
//    QueryWrapper queryWrapper = new QueryWrapper();

//    selectSeatMapper.selectList(queryWrapper);
  }

  // 更新订单状态 每分钟 超时设置为已超时
  @Transactional
  @Scheduled(cron = "0 * * * * ?")  // 每分钟执行
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_ORDER_STATE)
  public void updateMovieOrderState() {
    log.info("Updating movie order state...");
    QueryWrapper queryWrapper = new QueryWrapper();
    queryWrapper.eq("order_state", OrderState.order_created.getCode());

    List<MovieOrder> orderList =  movieOrderMapper.selectList(queryWrapper);

    List<MovieOrder> updateMovieOrderState = orderList.stream().filter(item -> {
      // 获取订单创建时间 (Date 类型)
      Date createTime = item.getCreateTime();

      // 将 Date 转换为 LocalDateTime
      LocalDateTime createTimeLocal = createTime.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();

      // 当前时间
      LocalDateTime now = LocalDateTime.now();

      // 检查创建时间是否超过15分钟
      return Duration.between(createTimeLocal, now).toMinutes() > Config.ORDER_TIMEOUT_MINUTES;
    }).map(item -> {
        item.setOrderState(OrderState.order_timeout.getCode());

        return  item;
      })
      .toList();

    if (updateMovieOrderState.size() > 0) {
      // 更新订单状态为超时
      movieOrderService.updateBatchById(updateMovieOrderState, updateMovieOrderState.size());

      // 更新选座状态为可用
      UpdateWrapper<SelectSeat> updateWrapper = new UpdateWrapper();

      updateWrapper
        .set("select_seat_state", SeatState.available.getCode())
        .in("movie_order_id", updateMovieOrderState.stream().map(item -> item.getId()).toList());

      selectSeatMapper.update(null, updateWrapper);
    }

  }
}