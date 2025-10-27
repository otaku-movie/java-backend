package com.example.backend.response.app;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import lombok.Data;

import java.util.List;

@Data
public class ShowTimeInfo {
  Integer id;
  Integer theaterHallId;
  String theaterHallName;
  String startTime;
  String endTime;
  String specName;
  Integer totalSeats;  // 新增：总座位数
  Integer selectedSeats;  // 新增：已选座位数
  Integer availableSeats;  // 新增：可用座位数（计算得出）
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  List<Language> subtitle;
  List<MovieShowTimeTag> showTimeTags;
}
