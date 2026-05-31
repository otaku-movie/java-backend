package com.example.backend.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.example.backend.service.OAuthIdTokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class OAuthIdTokenVerifierImpl implements OAuthIdTokenVerifier {
  private static final String GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs";
  private static final String APPLE_JWKS = "https://appleid.apple.com/auth/keys";

  @Value("${oauth.google.client-ids:}")
  private String googleClientIds;

  @Value("${oauth.apple.client-ids:}")
  private String appleClientIds;

  @Override
  public OAuthProfile verifyGoogle(String idToken) {
    JWTClaimsSet claims = verify(idToken, GOOGLE_JWKS, List.of("https://accounts.google.com", "accounts.google.com"), clientIds(googleClientIds));
    OAuthProfile profile = new OAuthProfile();
    profile.setProvider("google");
    profile.setSubject(claims.getSubject());
    profile.setEmail((String) claims.getClaim("email"));
    profile.setEmailVerified(parseBoolean(claims.getClaim("email_verified")));
    profile.setName((String) claims.getClaim("name"));
    profile.setPicture((String) claims.getClaim("picture"));
    return profile;
  }

  @Override
  public OAuthProfile verifyApple(String idToken, String expectedNonce) {
    JWTClaimsSet claims = verify(idToken, APPLE_JWKS, List.of("https://appleid.apple.com"), clientIds(appleClientIds));
    verifyNonce(claims, expectedNonce);
    OAuthProfile profile = new OAuthProfile();
    profile.setProvider("apple");
    profile.setSubject(claims.getSubject());
    profile.setEmail((String) claims.getClaim("email"));
    profile.setEmailVerified(parseBoolean(claims.getClaim("email_verified")));
    return profile;
  }

  /**
   * Apple idToken 中的 `nonce` claim 是客户端传给 Apple 的 sha256(nonce) 的原文。
   * 因此后端需要把客户端补传过来的原始 nonce 再做一次 sha256，base64url 编码后与 claim 对比。
   */
  private void verifyNonce(JWTClaimsSet claims, String expectedNonce) {
    if (!StringUtils.hasText(expectedNonce)) {
      // 兼容老客户端没传 nonce 的情况：不强制校验，但生产环境应监控并尽快下线老版本。
      return;
    }
    Object claim = claims.getClaim("nonce");
    if (!(claim instanceof String tokenNonce) || tokenNonce.isBlank()) {
      throw new IllegalArgumentException("id_token nonce missing");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(expectedNonce.getBytes(StandardCharsets.UTF_8));
      String hex = bytesToHex(hash);
      // Apple 文档：nonce claim 是 sha256(nonce) 的 hex 字符串（lower-case）。
      // 部分老客户端 / SDK 可能误传 base64url，这里两种都允许。
      String base64Url = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
      if (!tokenNonce.equalsIgnoreCase(hex) && !tokenNonce.equals(base64Url)) {
        throw new IllegalArgumentException("id_token nonce mismatch");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("id_token nonce verify failed", e);
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /** Google 多为 Boolean，Apple 多为 "true"/"false" 字符串，做统一解析。 */
  private Boolean parseBoolean(Object value) {
    if (value == null) return null;
    if (value instanceof Boolean b) return b;
    if (value instanceof String s && !s.isBlank()) return Boolean.parseBoolean(s.trim());
    return null;
  }

  private JWTClaimsSet verify(String idToken, String jwksUrl, List<String> issuers, List<String> audiences) {
    try {
      SignedJWT jwt = SignedJWT.parse(idToken);
      if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())) {
        throw new IllegalArgumentException("unsupported id_token algorithm");
      }
      JWKSet jwkSet = JWKSet.load(new URL(jwksUrl));
      JWK jwk = jwkSet.getKeyByKeyId(jwt.getHeader().getKeyID());
      if (!(jwk instanceof RSAKey rsaKey)) {
        throw new IllegalArgumentException("id_token key not found");
      }
      JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
      if (!jwt.verify(verifier)) {
        throw new IllegalArgumentException("id_token signature invalid");
      }

      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      if (!issuers.contains(claims.getIssuer())) {
        throw new IllegalArgumentException("id_token issuer invalid");
      }
      if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
        throw new IllegalArgumentException("id_token expired");
      }
      if (!audiences.isEmpty() && claims.getAudience().stream().noneMatch(audiences::contains)) {
        throw new IllegalArgumentException("id_token audience invalid");
      }
      if (!StringUtils.hasText(claims.getSubject())) {
        throw new IllegalArgumentException("id_token subject empty");
      }
      return claims;
    } catch (Exception e) {
      throw new IllegalArgumentException("id_token verify failed", e);
    }
  }

  private List<String> clientIds(String value) {
    if (!StringUtils.hasText(value)) return List.of();
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .toList();
  }
}
