package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.typeHandler.IntegerArrayTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@TableName("movie_show_time")
public class MovieShowTime {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("movie_id")
  Integer movieId;

  @TableField("cinema_id")
  Integer cinemaId;

  @TableField("theater_hall_id")
  Integer theaterHallId;

  @TableField("open")
  Boolean open;

  @TableField("start_time")
  String startTime ;

  @TableField("end_time")
  String endTime;

  @TableField("status")
  Integer status;

  @TableField(value = "subtitle_id", typeHandler = IntegerArrayTypeHandler.class)
  List<Integer> subtitleId;

  @TableField(value = "show_time_tag_id", typeHandler = IntegerArrayTypeHandler.class)
  List<Integer>  showTimeTagId;

  @TableField("spec_id")
  Integer  specId;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}