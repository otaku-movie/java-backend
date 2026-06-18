package com.example.backend.response.app;

import com.example.backend.response.showTime.ServiceDayResponse;
import lombok.Data;

import java.util.List;

@Data
public class DateGroup {
  String date;
  List<TheaterHallShowTime> data;
  /** 该日历日适用的服务日（按票种 schedule 规则匹配）。 */
  List<ServiceDayResponse> serviceDays;
}
