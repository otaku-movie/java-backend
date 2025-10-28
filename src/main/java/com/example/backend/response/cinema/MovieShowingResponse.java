package com.example.backend.response.cinema;

import lombok.Data;

@Data
public class MovieShowingResponse {
  Integer id;
  String name;
  String poster;
  Integer time;
  String levelName;
  // 新增：评分信息
  Double rate;           // 平均评分
  Integer totalRatings;  // 评分总人数
}
