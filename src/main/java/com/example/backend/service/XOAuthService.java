package com.example.backend.service;

/**
 * X (Twitter) OAuth 2.0 用户信息查询。
 *
 * <p>与 Google/Apple 不同，X 的 access_token 是不透明字符串而非 JWT，
 * 后端无法离线验签，必须调用 X 的 `/2/users/me` 接口确认 token 有效并取回用户信息。
 * 因此每次登录会有一次出网请求，是 X 流程的固有成本。</p>
 */
public interface XOAuthService {

  /**
   * 用客户端回传的 access_token 查询当前 X 用户信息。
   *
   * @return OAuth 通用 profile：provider=x，subject=X 用户 id（不会变）
   * @throws IllegalArgumentException token 无效 / 撤销 / 网络失败
   */
  OAuthIdTokenVerifier.OAuthProfile fetchProfile(String accessToken);
}
