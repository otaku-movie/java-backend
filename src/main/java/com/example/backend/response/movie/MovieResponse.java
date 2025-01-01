package com.example.backend.response.movie;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.example.backend.response.Spec;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MovieResponse {
  Integer id;
  String cover;
  String name;
  String originalName;
  String description;
  String homePage;
  String startDate;
  String endDate;
  // 1 未上映 2 上映中 3 上映结束
  Integer status;
  // 1 未上映 2 上映中 3 上映结束
  Integer time;
  Integer cinemaCount;
  Integer theaterCount;
  Integer commentCount;
  Integer watchedCount;
  Integer wantToSeeCount;

//  private Integer deleted;
  List<Spec> spec;
  List<HelloMovie> helloMovie;
  List<Tags> tags;

  Integer levelId;
  String levelName;
  String levelDescription;
  double rate;
  Integer totalRatings;
}
