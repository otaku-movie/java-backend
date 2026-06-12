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
  /** 当前登录用户是否已对该电影评分 */
  Boolean rated;
  /** 当前登录用户对该电影的评分（未登录或未评分时为 null） */
  Double userRate;

  /** 关联的预售券 id，有则可在 C 端跳转预售券详情 */
  Integer presaleId;
  /** 该预售券是否含特典 */
  Boolean hasBonus;

  /** 是否有入场者特典（普通上映，benefit 表存在记录即为 true） */
  Boolean hasBenefit;

  /** 是否存在重映计划（用于后台电影选择弹窗打标签/入口） */
  Boolean hasReRelease;
}
