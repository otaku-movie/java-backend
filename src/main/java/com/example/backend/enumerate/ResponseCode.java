package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum responseCode {
  @Getter
  success(1);

  private final Integer scucess;
}
