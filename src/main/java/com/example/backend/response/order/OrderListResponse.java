package com.example.backend.response.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
class Seat {
  Integer seat_x;
  Integer seat_y;
  String movie_ticket_type_name;
}

@Data
public class OrderListResponse {
  Integer id;
  BigDecimal order_total;
  Integer order_state;
  Integer pay_method;
  Integer pay_number;
  Integer pay_state;
  BigDecimal pay_total;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date order_time;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date pay_time;
  private String start_time;
  private String end_time;
  private Integer status;
  private Integer movie_id;
  private String movie_name;
  private String movie_poster;
  private Integer cinema_id;
  private String cinema_name;
  private String theater_hall_name;
  private String theater_hall_spec_name;
  List<Seat> seat;
}
