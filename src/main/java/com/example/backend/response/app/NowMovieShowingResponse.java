package com.example.backend.response.app;


import com.example.backend.response.Staff;
import com.example.backend.response.Spec;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.Tags;
import lombok.Data;

import java.util.List;

@Data
public class NowMovieShowingResponse {
  Integer id;
  String name;
  String cover;
  List<Tags> tags;
  List<Spec> spec;
  String levelName;
  List<Staff> cast;
  List<HelloMovie> helloMovie;
  String startDate;
  /** 是否有入场者特典（设计 3.4） */
  Boolean hasBenefits;
  /** 是否来自重映计划 */
  Boolean isReRelease;
  /** 关联重映计划ID（可空） */
  Integer reReleaseId;
  /** 重映特殊版本说明（可空） */
  String reReleaseVersionInfo;

  /** 关联的预售券 id（ムビチケ等），有则可跳转预售券详情 */
  Integer presaleId;
  /** 是否存在预售券 */
  Boolean hasPresaleTicket;
  /** 该预售券是否含特典 */
  Boolean hasBonus;
}
