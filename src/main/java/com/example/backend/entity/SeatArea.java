package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.enumerate.SeatType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seat_area")
public class SeatArea {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField(value = "theater_hall_id")
  Integer theaterHallId;

  @TableField("name")
  String name;

  @TableField("color")
  String color;

  @TableField("price")
  Integer price;

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
