package com.example.backend.response.app;

import lombok.Data;

/**
 * 下载页 - 各平台最新版本概览
 */
@Data
public class AppVersionLatestResponse {
  private AppVersionLatestItem android;
  private AppVersionLatestItem ios;
}
