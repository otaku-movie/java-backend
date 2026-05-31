package com.example.backend.service.impl;

import com.example.backend.service.OAuthIdTokenVerifier;
import com.example.backend.service.XOAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class XOAuthServiceImpl implements XOAuthService {

  /** X v2 用户信息端点。`user.fields` 决定附加字段。 */
  private static final String USER_ME_URL =
      "https://api.twitter.com/2/users/me?user.fields=id,name,username,profile_image_url";

  private final HttpClient httpClient;
  private final ObjectMapper jsonMapper;

  public XOAuthServiceImpl() {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    this.jsonMapper = new ObjectMapper();
  }

  @Override
  public OAuthIdTokenVerifier.OAuthProfile fetchProfile(String accessToken) {
    if (!StringUtils.hasText(accessToken)) {
      throw new IllegalArgumentException("X access_token is empty");
    }
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(USER_ME_URL))
        .timeout(Duration.ofSeconds(8))
        .header("Authorization", "Bearer " + accessToken)
        .header("Accept", "application/json")
        .GET()
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 401 || response.statusCode() == 403) {
        // token 过期 / 被撤销
        throw new IllegalArgumentException("X access_token unauthorized (HTTP " + response.statusCode() + ")");
      }
      if (response.statusCode() != 200) {
        throw new IllegalArgumentException(
            "X /2/users/me returned HTTP " + response.statusCode() + ": " + truncate(response.body()));
      }
      JsonNode root = jsonMapper.readTree(response.body());
      JsonNode data = root.path("data");
      String id = data.path("id").asText(null);
      if (!StringUtils.hasText(id)) {
        throw new IllegalArgumentException("X /2/users/me payload missing id");
      }
      String name = data.path("name").asText(null);
      String username = data.path("username").asText(null);
      String picture = data.path("profile_image_url").asText(null);

      OAuthIdTokenVerifier.OAuthProfile profile = new OAuthIdTokenVerifier.OAuthProfile();
      profile.setProvider("x");
      profile.setSubject(id);
      // X 不返回邮箱，不存在「邮箱已验证」概念，置 null 让 findOrCreateOAuthUser 走纯 (provider, subject) 绑定逻辑。
      profile.setEmail(null);
      profile.setEmailVerified(null);
      profile.setName(StringUtils.hasText(name) ? name : username);
      profile.setPicture(picture);
      return profile;
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new IllegalArgumentException("X /2/users/me request failed", e);
    }
  }

  /** 错误日志里把 X 返回的 HTML/长文本截断，避免污染日志。 */
  private static String truncate(String body) {
    if (body == null) return "";
    return body.length() <= 200 ? body : body.substring(0, 200) + "...";
  }
}
