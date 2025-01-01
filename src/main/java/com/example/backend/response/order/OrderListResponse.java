package com.example.backend.response.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
class Seat {
  Integer seatX;
  Integer seatY;
  String seatName;
  String movieTicketTypeName;
}

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
  private String date;
  private String startTime;
  private String endTime;
  private Integer status;
  private Integer movieId;
  private String movieName;
  private String moviePoster;
  private Integer cinemaId;
  private String cinemaName;
  private String theaterHallName;
  private String theaterHallSpecName;
  List<Seat> seat;
}
