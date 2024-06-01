package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.context.annotation.Primary;

import java.util.Date;

@Data
@TableName("movie")
public class Movie {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("cover")
  String cover;

  @TableField("name")
  String name;

  @TableField("original_name")
  String originalName;

  @TableField("description")
  String description;

  @TableField("level_id")
  Integer levelId;

  @TableField("home_page")
  String homePage;

  @TableField("start_date")
  String startDate;

  @TableField("end_date")
  String endDate;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("status")
  Integer status;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("time")
  Integer time;

  @TableField("watched_count")
  Integer watchedCount;

  @TableField("want_to_see_count")
  Integer wantToSeeCount;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
