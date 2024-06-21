package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.enumerate.SeatType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seat_aisle")
public class SeatAisle {
  @TableField(value = "theater_hall_id")
  Integer theaterHallId;

  @TableField("type")
  String type;

  @TableField("start")
  Integer start;

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
