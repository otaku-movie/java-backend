package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("select_seat")
public class SelectSeat {
  @TableField("user_id")
  Integer userId;

  @TableField("movie_show_time_id")
  Integer movieShowTimeId;

  @TableField("seat_id")
  Integer seatId;

  @TableField("select_seat_type")
  Integer selectSeatType;


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
