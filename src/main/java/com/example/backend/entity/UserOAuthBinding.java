package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * 用户 ↔ 第三方身份 (provider, subject) 的多对多绑定。
 *
 * <p>同一外部身份只能绑一个未删除用户（数据库 partial unique 索引保证）。
 * Google / Apple 等 OIDC provider 通用，新增 provider 不需改表结构。
 */
@Data
@TableName("user_oauth_binding")
public class UserOAuthBinding {
  @TableId(value = "id", type = IdType.AUTO)
  Long id;

  @TableField("user_id")
  Integer userId;

  /** google / apple / ... */
  @TableField("provider")
  String provider;

  /** id_token.sub */
  @TableField("subject")
  String subject;

  @TableField("email")
  String email;

  @TableField("name")
  String name;

  @TableField("picture")
  String picture;

  /** 可选：原始 profile JSON */
  @TableField("raw_profile")
  String rawProfile;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("last_login_at")
  Date lastLoginAt;

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
