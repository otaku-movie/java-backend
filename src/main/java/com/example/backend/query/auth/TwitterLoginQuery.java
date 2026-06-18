package com.example.backend.query.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * X (Twitter) OAuth 2.0 PKCE 登录请求。
 *
 * <p>客户端走 flutter_appauth 完成 PKCE 授权码 → token 交换，
 * 把拿到的 access_token 原样回传给后端。后端用它调 X /2/users/me 验真。</p>
 */
@Data
public class TwitterLoginQuery {
  /** X 颁发的 OAuth 2.0 access_token（PKCE 流程产物）。 */
  @NotEmpty
  private String accessToken;

  @NotEmpty
  private String deviceId;

  /** OAuth 首次建号时的注册来源：h5 / ios / android */
  private String registerSource;

  /** 本次登录来源：h5 / ios / android */
  private String loginSource;
}
