package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("movie_tag")
public class MovieTag {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("name")
  String name;

  // 译名列（name=日文原名）。仅供后端按语言选用，不直接对外序列化：
  // 接口仍只返回单个 name 字段（已按 Accept-Language 填好对应语言）。
  @JsonIgnore
  @TableField("name_zh")
  String nameZh;

  @JsonIgnore
  @TableField("name_en")
  String nameEn;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @JsonIgnore
  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
