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
}
