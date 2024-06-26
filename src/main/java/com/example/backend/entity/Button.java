package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("button")
public class Button {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("i18n_key")
  String i18nKey;

  @TableField("name")
  String name;

  @TableField("menu_id")
  Integer menuId;

  @TableField("api_code")
  String apiCode;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;
}
