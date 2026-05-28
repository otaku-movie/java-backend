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
import java.time.Instant;
import java.util.Arrays;
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
  public OAuthProfile verifyApple(String idToken) {
    JWTClaimsSet claims = verify(idToken, APPLE_JWKS, List.of("https://appleid.apple.com"), clientIds(appleClientIds));
    OAuthProfile profile = new OAuthProfile();
    profile.setProvider("apple");
    profile.setSubject(claims.getSubject());
    profile.setEmail((String) claims.getClaim("email"));
    profile.setEmailVerified(parseBoolean(claims.getClaim("email_verified")));
    return profile;
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
