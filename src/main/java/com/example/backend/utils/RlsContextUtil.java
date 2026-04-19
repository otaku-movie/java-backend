package com.example.backend.utils;

import com.example.backend.config.RequestContextHolder;
import com.example.backend.enumerate.DataScope;

/** 无 HTTP 上下文时（消息队列、定时任务等）为当前线程设置 RLS 所需的「平台级」可见范围。 */
public final class RlsContextUtil {

  private RlsContextUtil() {}

  public static void applyPlatformScope() {
    RequestContextHolder.setRls("level", DataScope.PLATFORM.getCode());
    RequestContextHolder.setRls("org_id", "");
    RequestContextHolder.setRls("cinema_ids", "");
    RequestContextHolder.setRls("user_id", "0");
  }

  public static void clearRls() {
    RequestContextHolder.clearRls();
  }
}
