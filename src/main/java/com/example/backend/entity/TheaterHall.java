package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("theater_hall")
public class TheaterHall {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("name")
  String name;

  @TableField("seat_count")
  Integer seatCount;

  @TableField("cinema_id")
  Integer cinemaId;

  @TableField("spec")
  Integer spec;

//  @TableField(value = "create_time", fill = FieldFill.INSERT)
//  Date createTime;
//
//  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
//  Date updateTime;
//
//  @TableLogic
//  @TableField(value = "deleted", fill = FieldFill.INSERT)
//  private Integer deleted;
}
