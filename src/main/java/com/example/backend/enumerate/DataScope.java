package com.example.backend.enumerate;

import lombok.Getter;

/** 后台用户数据范围，与 users.data_scope、RLS app.current_level 取值一致 */
@Getter
public enum DataScope {
  PLATFORM("platform"),
  CHAIN("chain"),
  CINEMA("cinema");

  private final String code;

  DataScope(String code) {
    this.code = code;
  }

  public static String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return PLATFORM.code;
    }
    String t = raw.trim().toLowerCase();
    for (DataScope s : values()) {
      if (s.code.equals(t)) {
        return s.code;
      }
    }
    return PLATFORM.code;
  }
}
