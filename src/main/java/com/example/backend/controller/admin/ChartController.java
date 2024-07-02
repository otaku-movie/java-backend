package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.mapper.*;
import com.example.backend.response.chart.ChartResponse;
import com.example.backend.response.chart.StatisticsUserCount;
import com.example.backend.utils.MessageUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChartController {
  @Autowired
  UserMapper userMapper;

  @Autowired
  MovieMapper movieMapper;

  @Autowired
  CinemaMapper cinemaMapper;

  @Autowired
  MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @GetMapping("/api/admin/chart")
  public RestBean<ChartResponse> chart () {
    ChartResponse chartResponse = new ChartResponse();
    QueryWrapper userQueryWrapper = new QueryWrapper();

    chartResponse.setUserCount(userMapper.selectCount(userQueryWrapper));
    chartResponse.setMovieCount(movieMapper.selectCount(userQueryWrapper));
    chartResponse.setCinemaCount(cinemaMapper.selectCount(userQueryWrapper));
    chartResponse.setShowTimeCount(movieShowTimeMapper.selectCount(userQueryWrapper));
    chartResponse.setStatisticsUserData(userMapper.StatisticsOfDailyRegisteredUsers());
    chartResponse.setStatisticsOfDailyMovieScreenings(movieShowTimeMapper.StatisticsOfDailyMovieScreenings());
    // 统计日订单数和订单状态
    chartResponse.setDailyOrderStatistics(movieOrderMapper.DailyOrderStatistics());
    // 统计日成交金额
    chartResponse.setDailyTransactionAmount(movieOrderMapper.DailyTransactionAmount(OrderState.order_succeed.getCode(), PayState.payment_successful.getCode()));

    return RestBean.success(chartResponse, MessageUtils.getMessage("success.get"));
  }
}
