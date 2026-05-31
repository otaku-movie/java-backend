package com.example.backend.service;

import lombok.Data;

public interface OAuthIdTokenVerifier {
  OAuthProfile verifyGoogle(String idToken);

  /**
   * 校验 Apple idToken。
   *
   * @param idToken    Apple 返回的 identityToken
   * @param expectedNonce 客户端发起登录时生成的随机 nonce 原文；
   *                      非空时会与 idToken 中的 `nonce` claim 进行 sha256 比对，
   *                      用于防止 idToken 被截获后重放。
   */
  OAuthProfile verifyApple(String idToken, String expectedNonce);

  @Data
  class OAuthProfile {
    private String subject;
    private String email;
    /**
     * provider 是否已确认该 email 归属当前用户：
     * - Google：claim 通常为 Boolean
     * - Apple：claim 通常为 String "true"/"false"
     * - 不存在 / 解析不出 → null（视作未验证，调用方应避免按 email 自动合并已有账号）
     */
    private Boolean emailVerified;
    private String name;
    private String picture;
    private String provider;
  }
}
