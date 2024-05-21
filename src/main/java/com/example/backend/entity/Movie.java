package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.context.annotation.Primary;

import java.util.Date;

@Data
@TableName("movie")
public class Movie {
  @TableId(type = IdType.AUTO)
  int id;

  @TableField("cover")
  String cover;

  @TableField("name")
  String name;

  @TableField("description")
  String description;

  @TableField("home_page")
  String homePage;

  @TableField("start_date")
  String startDate;

  @TableField("end_date")
  String endDate;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("status")
  int status;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("time")
  int time;

  @TableField("comment_count")
  int commentCount;

  @TableField("watched_count")
  int watchedCount;

  @TableField("want_to_see_count")
  int wantToSeeCount;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  String createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  String updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
