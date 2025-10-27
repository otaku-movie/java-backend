package com.example.backend.response.app;

import lombok.Data;

import java.util.List;

@Data
public class DateGroup {
  String date;
  List<TheaterHallShowTime> data;
}
