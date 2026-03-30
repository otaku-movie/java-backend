package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("app_version")
public class AppVersion {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("platform")
  String platform;

  @TableField("version_code")
  Integer versionCode;

  @TableField("version_name")
  String versionName;

  @TableField("build_number")
  Integer buildNumber;

  @TableField("download_url")
  String downloadUrl;

  @TableField("update_message")
  String updateMessage;

  @TableField("force_update")
  Boolean forceUpdate;

  @TableField("is_force_update")
  Boolean isForceUpdate;

  @TableField("min_supported_version")
  String minSupportedVersion;

  @TableField("is_latest")
  Boolean isLatest;

  @TableField("release_note_zh")
  String releaseNoteZh;

  @TableField("release_note_ja")
  String releaseNoteJa;

  @TableField("release_note_en")
  String releaseNoteEn;

  @TableField("release_note_internal")
  String releaseNoteInternal;

  @TableField("release_percent")
  Integer releasePercent;

  @TableField("deleted")
  Integer deleted;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;
}
