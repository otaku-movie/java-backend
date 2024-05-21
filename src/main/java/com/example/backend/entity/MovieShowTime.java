package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

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

  @TableField("start_time")
  Date startTimee ;

  @TableField("end_time")
  Date endTime;

  @TableField("status")
  Integer status;
}