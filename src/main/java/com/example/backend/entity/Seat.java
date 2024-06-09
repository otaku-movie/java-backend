package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seat")
public class Seat {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField(value = "theater_hall_id")
  Integer theaterHallId;

  @TableField("x_name")
  String xName;

  @TableField("x_axis")
  Integer xAxis;

  @TableField("y_axis")
  Integer yAxis;

  @TableField("z_axis")
  Integer zAxis;

  @TableField("seat_type")
  Integer seatType;

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
