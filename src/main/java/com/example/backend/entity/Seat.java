package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("seat")
public class Seat {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

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
