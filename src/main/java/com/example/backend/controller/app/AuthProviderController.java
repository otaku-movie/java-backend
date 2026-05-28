package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.AuthProviderConfig;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.AuthProviderConfigMapper;
import com.example.backend.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("appAuthProviderController")
public class AuthProviderController {
  @Autowired
  private AuthProviderConfigMapper authProviderConfigMapper;

  @GetMapping(ApiPaths.App.AuthProvider.LIST)
  public RestBean<List<AuthProviderConfig>> list(
      @RequestParam(required = false) String platform,
      @RequestParam(required = false) String appVersion
  ) {
    QueryWrapper<AuthProviderConfig> wrapper = new QueryWrapper<AuthProviderConfig>()
        .eq("deleted", 0)
        .eq("enabled", true)
        .and(w -> w.eq("platform", "ALL").or().eq("platform", normalizePlatform(platform)))
        .orderByAsc("sort")
        .orderByAsc("id");

    List<AuthProviderConfig> providers = authProviderConfigMapper.selectList(wrapper)
        .stream()
        .filter(item -> isVersionSupported(appVersion, item.getMinAppVersion()))
        .toList();

    return RestBean.success(providers, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  private String normalizePlatform(String platform) {
    if (!StringUtils.hasText(platform)) return "ALL";
    String p = platform.trim().toLowerCase();
    if (p.contains("ios")) return "IOS";
    if (p.contains("android")) return "Android";
    return platform.trim();
  }

  private boolean isVersionSupported(String current, String min) {
    if (!StringUtils.hasText(min) || !StringUtils.hasText(current)) return true;
    return compareVersion(current, min) >= 0;
  }

  /** return >0 if v1>v2, 0 if equal, <0 if v1<v2 */
  private int compareVersion(String v1, String v2) {
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
