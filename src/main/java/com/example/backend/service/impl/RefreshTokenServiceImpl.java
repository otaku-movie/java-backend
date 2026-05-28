package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.response.AppLoginResponse;
import com.example.backend.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private static final String REFRESH_PREFIX = "auth:refresh:";
  private static final String DEVICE_PREFIX = "auth:refresh-device:";

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private UserMapper userMapper;

  @Override
  public AppLoginResponse issueLoginResponse(User user, String deviceId) {
    StpUtil.login(user.getId());
    String accessToken = StpUtil.getTokenValue();
    String refreshToken = rotateRefreshToken(user.getId(), deviceId);

    AppLoginResponse response = new AppLoginResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setCover(user.getCover());
    response.setCreateTime(user.getCreateTime());
    response.setToken(accessToken);
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    response.setAccessExpiresIn(ACCESS_EXPIRES_IN_SECONDS);
    response.setRefreshExpiresIn(REFRESH_EXPIRES_IN_SECONDS);
    return response;
  }

  @Override
  public AppLoginResponse refresh(String refreshToken, String deviceId) {
    String key = REFRESH_PREFIX + sha256(refreshToken);
    String userId = redisTemplate.opsForValue().get(key);
    if (userId == null) {
      throw new IllegalArgumentException("refresh token expired");
    }

    String expectedDevice = redisTemplate.opsForValue().get(DEVICE_PREFIX + userId + ":" + deviceId);
    if (expectedDevice == null || !expectedDevice.equals(sha256(refreshToken))) {
      throw new IllegalArgumentException("refresh token invalid");
    }

    redisTemplate.delete(key);
    User user = userMapper.selectOne(new QueryWrapper<User>()
        .eq("id", Integer.parseInt(userId))
        .eq("deleted", 0)
        .last("LIMIT 1"));
    if (user == null) {
      throw new IllegalArgumentException("user not found");
    }

    return issueLoginResponse(user, deviceId);
  }

  @Override
  public void revoke(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) return;
    String hash = sha256(refreshToken);
    String userId = redisTemplate.opsForValue().get(REFRESH_PREFIX + hash);
    redisTemplate.delete(REFRESH_PREFIX + hash);
    if (userId != null) {
      redisTemplate.keys(DEVICE_PREFIX + userId + ":*").forEach(redisTemplate::delete);
    }
  }

  @Override
  public void revokeAllForUser(Integer userId) {
    if (userId == null) return;
    var keys = redisTemplate.keys(DEVICE_PREFIX + userId + ":*");
    if (keys == null || keys.isEmpty()) return;
    keys.forEach(deviceKey -> {
      String hash = redisTemplate.opsForValue().get(deviceKey);
      if (hash != null) {
        redisTemplate.delete(REFRESH_PREFIX + hash);
      }
      redisTemplate.delete(deviceKey);
    });
  }

  private String rotateRefreshToken(Integer userId, String deviceId) {
    String token = UUID.randomUUID() + "." + UUID.randomUUID();
    String hash = sha256(token);
    String deviceKey = DEVICE_PREFIX + userId + ":" + deviceId;
    String oldHash = redisTemplate.opsForValue().get(deviceKey);
    if (oldHash != null) {
      redisTemplate.delete(REFRESH_PREFIX + oldHash);
    }
    redisTemplate.opsForValue().set(REFRESH_PREFIX + hash, userId.toString(), REFRESH_EXPIRES_IN_SECONDS, TimeUnit.SECONDS);
    redisTemplate.opsForValue().set(deviceKey, hash, REFRESH_EXPIRES_IN_SECONDS, TimeUnit.SECONDS);
    return token;
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
