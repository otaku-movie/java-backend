package com.example.backend.response.chart;

import lombok.Data;

import java.util.List;

@Data
class OrderState {
  Integer state;
  Integer count;
}

@Data
public class DailyOrderStatistics {
  String date;
  Long count;
  List<OrderState> orderState;
}
