package com.example.backend.controller.app;

import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AppVersionMapper;
import com.example.backend.response.app.AppVersionCheckResponse;
import com.example.backend.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("appAppVersionController")
public class AppVersionController {
  @Autowired
  private AppVersionMapper appVersionMapper;

  @GetMapping(ApiPaths.App.Version.CHECK)
  public RestBean<AppVersionCheckResponse> check(
      @RequestParam String platform,
      @RequestParam String version,
      @RequestParam(required = false) Integer buildNumber,
      @RequestHeader(value = "Accept-Language", required = false) String lang
  ) {
    String normalizedPlatform = normalizePlatform(platform);
    AppVersionCheckResponse latest = appVersionMapper.latestForCheck(normalizedPlatform, lang == null ? "zh-CN" : lang);
    if (latest == null) {
      AppVersionCheckResponse out = new AppVersionCheckResponse();
      out.setLatestVersion(version);
      out.setLatestBuildNumber(buildNumber == null ? 0 : buildNumber);
      out.setNeedUpdate(false);
      out.setForceUpdate(false);
      return RestBean.success(out, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
    }

    int currentBuild = buildNumber == null ? 0 : buildNumber;
    int latestBuild = latest.getLatestBuildNumber() == null ? 0 : latest.getLatestBuildNumber();
    boolean needUpdate = latestBuild > currentBuild;
    if (!needUpdate && latestBuild == 0) {
      needUpdate = compareVersion(latest.getLatestVersion(), version) > 0;
    }
    boolean forceByMin = false;
    if (latest.getMinSupportedVersion() != null && !latest.getMinSupportedVersion().isEmpty()) {
      forceByMin = compareVersion(version, latest.getMinSupportedVersion()) < 0;
    }

    latest.setNeedUpdate(needUpdate);
    latest.setForceUpdate(forceByMin || Boolean.TRUE.equals(latest.getForceUpdate()));
    return RestBean.success(latest, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  private String normalizePlatform(String platform) {
    if (platform == null) return "Android";
    String p = platform.trim().toLowerCase();
    if (p.contains("ios")) return "IOS";
    if (p.contains("android")) return "Android";
    return platform;
  }

  /** return >0 if v1>v2, 0 if equal, <0 if v1<v2 */
  private int compareVersion(String v1, String v2) {
    if (v1 == null && v2 == null) return 0;
    if (v1 == null) return -1;
    if (v2 == null) return 1;
    String[] a = v1.split("\\.");
    String[] b = v2.split("\\.");
    int n = Math.max(a.length, b.length);
    for (int i = 0; i < n; i++) {
      int x = i < a.length ? safeInt(a[i]) : 0;
      int y = i < b.length ? safeInt(b[i]) : 0;
      if (x != y) return Integer.compare(x, y);
    }
    return 0;
  }

  private int safeInt(String s) {
    try {
      return Integer.parseInt(s.replaceAll("[^0-9]", ""));
    } catch (Exception ignored) {
      return 0;
    }
  }
}

