package com.example.backend.response.app;

import lombok.Data;

@Data
public class AppVersionCheckResponse {
  private String latestVersion;
  private Integer latestBuildNumber;
  private Boolean forceUpdate;
  private Boolean needUpdate;
  private String downloadUrl;
  private String releaseNote;
  private String minSupportedVersion;
}

