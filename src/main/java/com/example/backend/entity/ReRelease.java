package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("re_release")
public class ReRelease {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("movie_id")
  Integer movieId;

  @TableField("start_date")
  Date startDate;

  @TableField("end_date")
  Date endDate;

  /** 特殊版本说明/备注（可为空） */
  @TableField("version_info")
  String versionInfo;

  /** 可选：覆盖展示名（为空则复用 movie.name） */
  @TableField("display_name_override")
  String displayNameOverride;

  /** 可选：覆盖海报（为空则复用 movie.cover） */
  @TableField("poster_override")
  String posterOverride;

  /** 1=启用 0=停用 */
  @TableField("status")
  Integer status;

  /** 可选：覆盖片长（分钟，为空则复用 movie.time） */
  @TableField("time_override")
  Integer timeOverride;


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
