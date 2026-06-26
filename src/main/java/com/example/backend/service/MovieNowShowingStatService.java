package com.example.backend.service;

import com.example.backend.mapper.MovieNowShowingStatMapper;
import com.example.backend.utils.RlsContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 正在上映列表的场次统计预聚合刷新。
 * 读路径走 {@code movie_now_showing_stat}，避免每次 API 扫描全量未来场次。
 */
@Slf4j
@Service
public class MovieNowShowingStatService {

  private final AtomicBoolean refreshing = new AtomicBoolean(false);

  @Autowired
  private MovieNowShowingStatMapper statMapper;

  public void refreshIfIdle() {
    if (!refreshing.compareAndSet(false, true)) {
      log.debug("movie_now_showing_stat refresh skipped: already running");
      return;
    }
    RlsContextUtil.applyPlatformScope();
    long t0 = System.currentTimeMillis();
    try {
      int rows = statMapper.rebuildAll();
      log.info("movie_now_showing_stat refreshed: {} movies in {}ms", rows, System.currentTimeMillis() - t0);
    } catch (Exception e) {
      log.error("movie_now_showing_stat refresh failed", e);
    } finally {
      RlsContextUtil.clearRls();
      refreshing.set(false);
    }
  }

  @Async
  @EventListener(ApplicationReadyEvent.class)
  public void warmOnStartup() {
    refreshIfIdle();
  }
}
