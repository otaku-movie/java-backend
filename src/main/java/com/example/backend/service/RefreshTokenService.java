package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.response.AppLoginResponse;

public interface RefreshTokenService {
  long ACCESS_EXPIRES_IN_SECONDS = 2 * 60 * 60L;
  long REFRESH_EXPIRES_IN_SECONDS = 30 * 24 * 60 * 60L;

  AppLoginResponse issueLoginResponse(User user, String deviceId);

  AppLoginResponse refresh(String refreshToken, String deviceId);

  void revoke(String refreshToken);

  void revokeAllForUser(Integer userId);
}
