package com.example.backend.response.chart;

import lombok.Data;

@Data
public class DailyTransactionAmount {
  String date;
  Long total_amount;
}
