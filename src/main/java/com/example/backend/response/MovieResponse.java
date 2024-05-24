package com.example.backend.response;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
class Spec {
  Integer id;
  String name;
  String description;
}
@Data
public class MovieResponse {
  Integer id;
  String cover;
  String name;
  String original_name;
  String description;
  String home_page;
  String start_date;
  String end_date;
  // 1 未上映 2 上映中 3 上映结束
  int status;
  // 1 未上映 2 上映中 3 上映结束
  int time;
  int comment_count;
  int watched_count;
  int want_to_see_count;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date create_time;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date update_time;

//  private Integer deleted;
  List<Spec> spec;
}