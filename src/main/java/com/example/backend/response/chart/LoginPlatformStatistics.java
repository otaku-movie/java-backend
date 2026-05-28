package com.example.backend.response.chart;

import lombok.Data;

/**
 * 按登录/注册平台聚合的用户数统计。
 *
 * <p>分类基于 user_oauth_binding 表：
 * 无绑定 → local，单 provider → 该 provider 名（google / apple ...），
 * 多 provider → mixed。
 */
@Data
public class LoginPlatformStatistics {
  String platform;
  Long userCount;
}
