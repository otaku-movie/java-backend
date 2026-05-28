package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("agreement")
public class Agreement {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("code")
  private String code;

  @TableField("language")
  private String language;

  @TableField("title")
  private String title;

  @TableField("content")
  private String content;

  @TableField("version")
  private String version;

  @TableField("status")
  private String status;

  @TableField("is_required_accept")
  private Boolean isRequiredAccept;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("effective_at")
  private Date effectiveAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("published_at")
  private Date publishedAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
