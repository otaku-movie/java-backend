package com.example.backend.response.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class MyTicketsResponse {
  Integer id;
  /** 订单号（用于跳转订单详情等） */
  String orderNumber;
  BigDecimal orderTotal;
  Integer orderState;
  String payMethod;
  Integer payNumber;
  Integer payState;
  Integer refundState;
  Integer refundApplyStatus;
  BigDecimal payTotal;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date orderTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date payTime;
  String date;
  String startTime;
  String endTime;
  Integer movieId;
  String movieName;
  String originalName;
  String moviePoster;
  Integer cinemaId;
  String cinemaName;
  String cinemaFullAddress;
  String cinemaTel;
  Integer theaterHallId;
  String theaterHallName;
  /** MyBatis 映射用，序列化时忽略，前端使用 specNames */
  @JsonIgnore
  String specName;
  /** 规格名称列表（多个），由 Service 从 specName 拆分填充 */
  List<String> specNames;
  /** 放映类型：1=2D，2=3D */
  Integer dimensionType;
  Integer movieShowTimeId;  // 场次ID（用于从 Redis 获取座位状态）
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date payDeadline;  // 支付截止时间（用于前端倒计时显示）
  List<MovieOrderSeat> seat;
}
