package com.example.backend.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("cover")
  String cover;

  @TableField("name")
  String name;

  @JsonIgnore
  @TableField("password")
  String password;

  @TableField("email")
  String email;

  /** platform / chain / cinema，见 {@link com.example.backend.enumerate.DataScope} */
  @TableField("data_scope")
  String dataScope;

  /** data_scope=chain 时所属品牌 */
  @TableField("brand_id")
  Integer brandId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @JsonIgnore
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
