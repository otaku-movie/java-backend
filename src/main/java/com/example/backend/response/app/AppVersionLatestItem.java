package com.example.backend.response.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 下载页 - 单个平台最新版本信息
 */
@Data
public class AppVersionLatestItem {
  /** 平台: Android / IOS */
  private String platform;
  /** 版本号，例如 1.2.0 */
  private String versionName;
  /** Build 号 */
  private Integer buildNumber;
  /** 下载地址（直链或商店地址） */
  private String downloadUrl;
  /** 根据 Accept-Language 选择的更新日志（Markdown） */
  private String releaseNote;
  /** 最低支持版本 */
  private String minSupportedVersion;
  /** 是否强制更新（仅供前端展示提示用） */
  private Boolean forceUpdate;
  /** 上架时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date publishedAt;
}
