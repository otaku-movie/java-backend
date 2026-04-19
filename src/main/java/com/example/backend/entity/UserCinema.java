package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_cinema")
public class UserCinema {

  @TableField("user_id")
  private Integer userId;

  @TableField("cinema_id")
  private Integer cinemaId;
}
