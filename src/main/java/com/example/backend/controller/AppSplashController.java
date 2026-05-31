package com.example.backend.controller;

import com.example.backend.config.AppSplashProperties;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.RestBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * App 启动页（splash）配置接口。
 *
 * <p>App 进入冷启动时调用本接口拉取最新的启动图 / 文案 / 时长，
 * 客户端会缓存到 SharedPreferences 供下一次启动 0 延迟使用。</p>
 *
 * <p>当前实现从配置文件读取；后续接 DB 时只需把字段来源换成 Mapper 查询，
 * 接口契约保持不变。</p>
 */
@RestController
public class AppSplashController {

  private final AppSplashProperties props;

  public AppSplashController(AppSplashProperties props) {
    this.props = props;
  }

  @GetMapping(ApiPaths.App.Splash.CURRENT)
  public RestBean<Map<String, Object>> current() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("imageUrl", emptyToNull(props.getImageUrl()));
    data.put("titleEn", props.getTitleEn());
    data.put("titleZh", props.getTitleZh());
    data.put("titleJa", props.getTitleJa());
    data.put("subtitleEn", props.getSubtitleEn());
    data.put("subtitleZh", props.getSubtitleZh());
    data.put("subtitleJa", props.getSubtitleJa());
    data.put("minDurationMs", props.getMinDurationMs());
    data.put("maxDurationMs", props.getMaxDurationMs());
    data.put("tapAction", emptyToNull(props.getTapAction()));
    return RestBean.success(data, "ok");
  }

  private static String emptyToNull(String s) {
    return s == null || s.isBlank() ? null : s;
  }
}
