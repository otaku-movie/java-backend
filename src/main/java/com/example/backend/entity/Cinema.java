package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("cinema")
public class Cinema {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("name")
  String name;

  @TableField("description")
  String description;

  @TableField("address")
  String address;

  @TableField("tel")
  String tel;

  @TableField("home_page")
  String homePage;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
