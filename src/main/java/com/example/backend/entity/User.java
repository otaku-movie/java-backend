package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("cover")
  String cover;

  @TableField("username")
  String username;

  @TableField("password")
  String password;

  @TableField("email")
  String email;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

//  @TableField(value = "deleted", fill = FieldFill.INSERT)
//  private Integer deleted;
}
