package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("select_seat")
public class SelectSeat {
  @TableField("user_id")
  Integer userId;

  @TableField("movie_order_id")
  Integer movieOrderId;

  @TableField("movie_ticket_type_id")
  Integer movieTicketTypeId;

  @TableField("movie_show_time_id")
  Integer movieShowTimeId;

  @TableField("theater_hall_id")
  Integer theaterHallId;

  @TableField("x")
  Integer x;

  @TableField("y")
  Integer y;

  @TableField("select_seat_state")
  Integer selectSeatState;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
