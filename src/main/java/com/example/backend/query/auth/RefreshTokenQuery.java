package com.example.backend.query.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RefreshTokenQuery {
  @NotEmpty
  private String refreshToken;

  @NotEmpty
  private String deviceId;
}
