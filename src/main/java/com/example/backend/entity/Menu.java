package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("menu")
public class Menu {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("i18n_key")
  String i18nKey;

  @TableField("name")
  String name;

  @TableField("show")
  Boolean show;

  @TableField("path")
  String path;

  @TableField("path_name")
  String pathName;

  @TableField("parent_id")
  Integer parentId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;
}
