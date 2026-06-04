package com.example.backend.utils;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 用户密码哈希工具。
 *
 * <p>当前标准方案：前端提交 <b>明文</b>（依赖 HTTPS 保护传输），后端用
 * <b>BCrypt（自带随机盐 + 慢哈希）</b> 存储，即 {@code bcrypt(明文)}。
 *
 * <p>历史遗留：早期前端会先对明文做一次 md5 再提交，后端再 md5 一次，
 * 库内存的是无盐 {@code md5(md5(明文))}（也可能是单层 {@code md5(明文)}）。
 * 为避免重置存量账号，{@link #matches} 会兼容这两种老格式；一旦校验通过，
 * 调用方应通过 {@link #needsUpgrade} 判断并用 {@link #encode} 重哈希为 BCrypt。
 */
public final class PasswordUtil {

  /** BCrypt 强度（cost factor），10 为常用且性能可接受的默认值。 */
  private static final int BCRYPT_COST = 10;

  private PasswordUtil() {}

  /** 是否为 BCrypt 哈希（$2a$ / $2b$ / $2y$ 前缀）。 */
  public static boolean isBcrypt(String stored) {
    return stored != null
        && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
  }

  /** 库内密码是否仍是老格式（非 BCrypt），需要在下次校验通过后升级。 */
  public static boolean needsUpgrade(String stored) {
    return stored != null && !stored.isBlank() && !isBcrypt(stored);
  }

  /** 把前端提交的明文密码编码为可存储的 BCrypt 哈希。 */
  public static String encode(String rawPlain) {
    return BCrypt.hashpw(rawPlain, BCrypt.gensalt(BCRYPT_COST));
  }

  /**
   * 校验前端提交的明文密码是否匹配库内存储值。
   *
   * <p>兼容三种历史格式：BCrypt、单层 {@code md5(明文)}、双层 {@code md5(md5(明文))}。
   *
   * @param rawPlain 前端提交的明文密码
   * @param stored   库内存储的哈希
   */
  public static boolean matches(String rawPlain, String stored) {
    if (rawPlain == null || stored == null || stored.isBlank()) {
      return false;
    }
    if (isBcrypt(stored)) {
      try {
        return BCrypt.checkpw(rawPlain, stored);
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
    // 历史无盐 md5：兼容单层与双层（早期前端先 md5 一次，后端再 md5 一次）
    String md5Once = SaSecureUtil.md5(rawPlain);
    String md5Twice = SaSecureUtil.md5(md5Once);
    return stored.equalsIgnoreCase(md5Once) || stored.equalsIgnoreCase(md5Twice);
  }
}
