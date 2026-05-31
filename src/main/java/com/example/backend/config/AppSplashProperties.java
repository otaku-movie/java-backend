package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * App 启动页（splash）运营可配置项。
 *
 * <p>当前实现：从 {@code application.yml} 的 {@code app.splash.*} 读取。
 * 运营改图改文案需要重启后端（dev 环境 spring-boot-devtools 会自动 reload）。</p>
 *
 * <p>下一步可演进方向：</p>
 * <ul>
 *   <li>建表 {@code app_splash_config}，按 (start_at, end_at) 多档期排程</li>
 *   <li>Admin 上传新图后写表，{@link com.example.backend.controller.AppSplashController}
 *       从 DB 取当前生效行</li>
 *   <li>新档期上线时通过 Redis pub/sub 通知所有节点失效本地缓存</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "app.splash")
@Data
public class AppSplashProperties {
  /** 远程背景图 URL；为空时客户端会用 bundled 的兜底图。 */
  private String imageUrl;
  private String titleEn;
  private String titleZh;
  private String titleJa;
  private String subtitleEn;
  private String subtitleZh;
  private String subtitleJa;
  /** 最短展示时长（毫秒）。客户端用来避免一闪而过。 */
  private int minDurationMs = 1500;
  /** 最长展示时长（毫秒）。后台慢/无网时，客户端最多等这么久就强制进首页。 */
  private int maxDurationMs = 3500;
  /** 点击启动图的动作：{@code movie:{id}} / {@code url:{https://...}} / 空 = 不响应。 */
  private String tapAction;
}
