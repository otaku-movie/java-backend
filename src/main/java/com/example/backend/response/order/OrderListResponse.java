package com.example.backend.response.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderListResponse {
  Integer id;
  BigDecimal orderTotal;
  Integer orderState;
  String payMethod;
  Integer payNumber;
  Integer payState;
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
  String moviePoster;
  Integer cinemaId;
  String cinemaName;
  String theaterHallName;
  String specName;
  List<MovieOrderSeat> seat;
}
