package com.example.backend.query.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OAuthLoginQuery {
  /** Google idToken / Apple identityToken */
  @NotEmpty
  private String idToken;

  @NotEmpty
  private String deviceId;

  /**
   * Apple Sign-In 防重放用的随机 nonce 原文。
   *
   * 客户端调用 Apple 时应传入 sha256(nonce)，后端会再对原始 nonce 做哈希后与
   * idToken 中的 `nonce` claim 比对，确保 idToken 不是被截获重放的旧 token。
   *
   * Google 流程目前不依赖 nonce，因此该字段可空。
   */
  private String nonce;

  /**
   * 首次 Apple 登录时客户端拿到的名字（Apple 只在首次登录时返回 fullName）。
   * 后端拿来填充 User.name，下次登录就拿不到了。
   */
  private String firstName;

  /** 同上，姓氏。 */
  private String lastName;
}
