package com.example.backend.constants;

/**
 * C 端用户注册来源（写入 users.register_source）与登录来源（写入 users.last_login_source）。
 *
 * <p>由客户端在注册 / OAuth 首次建号时上报；历史数据为 null，统计时视为 unknown。</p>
 */
public final class RegisterSource {

  private RegisterSource() {}

  public static final String H5 = "h5";
  public static final String IOS = "ios";
  public static final String ANDROID = "android";
  public static final String UNKNOWN = "unknown";

  /** 归一化客户端上报值；空或无法识别 → {@link #UNKNOWN}。 */
  public static String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return UNKNOWN;
    }
    String s = raw.trim().toLowerCase();
    return switch (s) {
      case "h5", "web", "mobile-web", "movie-h5", "movie_h5" -> H5;
      case "ios", "iphone", "ipad" -> IOS;
      case "android" -> ANDROID;
      case "unknown" -> UNKNOWN;
      default -> s.length() <= 16 ? s : UNKNOWN;
    };
  }
}
