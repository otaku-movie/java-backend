package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.mapper.*;
import com.example.backend.response.chart.ChartResponse;
import com.example.backend.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@RestController
public class ChartController {
  @Autowired
  UserMapper userMapper;

  @Autowired
  MovieMapper movieMapper;

  @Autowired
  CinemaMapper cinemaMapper;

  @Autowired
  MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @Autowired
  ChartMapper chartMapper;

  /**
   * 这些聚合都是只读统计，慢的原因是 19 条 SQL 全部串行执行（其中 7 条都扫 movie_show_time，
   * varchar start_time 上 cast 后无法走索引）。一次性扫表 ~100-300ms × 19 条 = 4-5s。
   *
   * 优化分 4 层（叠加效果）：
   *  1) ChartMapper.xml / MovieShowTimeMapper.xml 里的 SQL 改成能走索引（前缀范围 + MATERIALIZED CTE）；
   *  2) 19 条独立 SQL 用专用 IO 线程池并行（HikariCP 池调到 20，刚好同时容纳）；
   *  3) 响应缓存 60s + Stale-While-Revalidate：返回当前 cache 立刻响应，到期时只有第一个请求触发后台重算；
   *  4) 应用启动后立即预热一次 + 每 45s 后台定时刷新，让前端访问基本永远命中。
   */
  private static final Duration CACHE_TTL = Duration.ofSeconds(60);
  /**
   * 实例级线程池（不再用 static）：static 池不会随 Spring 容器销毁而关闭，配合 DevTools
   * 自动重启会每次泄漏一批 chart-aggregator 线程，长时间运行最终耗尽原生内存导致 JVM 崩溃。
   * 改为实例字段 + {@link #shutdownExecutor()} 的 @PreDestroy，容器（含 DevTools 重启）关闭时一并回收。
   *
   * <p>同时用「核心线程也允许 60s 空闲超时 + SynchronousQueue」：19 条 SQL 短时并发跑完后，
   * 空闲线程会自动退出，不再常驻 20 个线程。最大并发仍限制在 20，刚好不超过 HikariCP 池大小。
   */
  private final ExecutorService chartExecutor = buildChartExecutor();
  private final AtomicReference<CachedChart> cache = new AtomicReference<>();
  // 保证同一时间只有一个后台 refresh 在跑，避免缓存到期时多个请求同时触发重算
  private final java.util.concurrent.atomic.AtomicBoolean refreshing =
      new java.util.concurrent.atomic.AtomicBoolean(false);

  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.Chart.DATA)
  public RestBean<ChartResponse> chart () {
    ChartResponse chartResponse = loadCached();
    return RestBean.success(chartResponse, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /**
   * Stale-While-Revalidate：
   *  - 有任何 cache（即便过期）→ 立即返回，并触发一次后台异步刷新；
   *  - 完全没有 cache（冷启动场景）→ 同步算一次。
   * 这样用户永远不会因为 cache 到期等 19 条 SQL。
   */
  private ChartResponse loadCached () {
    CachedChart current = cache.get();
    if (current == null) {
      ChartResponse fresh = buildResponse();
      cache.set(new CachedChart(fresh, Instant.now()));
      return fresh;
    }
    if (current.isExpired()) {
      triggerAsyncRefresh();
    }
    return current.response;
  }

  private void triggerAsyncRefresh () {
    if (!refreshing.compareAndSet(false, true)) {
      return;
    }
    CompletableFuture.runAsync(() -> {
      try {
        cache.set(new CachedChart(buildResponse(), Instant.now()));
      } catch (Exception e) {
        log.warn("chart cache refresh failed: {}", e.getMessage());
      } finally {
        refreshing.set(false);
      }
    }, chartExecutor);
  }

  /**
   * 应用启动完成后立即跑一次，让第一次访问就命中 cache。
   * 用 ApplicationReadyEvent 比 @PostConstruct 更安全：所有 bean / 数据源 / mapper 都已就绪。
   */
  @EventListener(ApplicationReadyEvent.class)
  public void warmupOnStart () {
    try {
      cache.set(new CachedChart(buildResponse(), Instant.now()));
      log.info("chart cache warmed up on startup");
    } catch (Exception e) {
      log.warn("chart cache warmup failed: {}", e.getMessage());
    }
  }

  /** 每 45s 后台主动刷一遍（< 60s TTL），覆盖 SWR 触发不到的情况。 */
  @Scheduled(fixedDelay = 45_000L, initialDelay = 45_000L)
  public void scheduledRefresh () {
    triggerAsyncRefresh();
  }

  private ChartResponse buildResponse () {
    QueryWrapper userQueryWrapper = new QueryWrapper();

    CompletableFuture<Long> userCount = async(() -> userMapper.selectCount(userQueryWrapper));
    CompletableFuture<Long> movieCount = async(() -> movieMapper.selectCount(userQueryWrapper));
    CompletableFuture<Long> cinemaCount = async(() -> cinemaMapper.selectCount(userQueryWrapper));
    CompletableFuture<Long> showTimeCount = async(() -> movieShowTimeMapper.selectCount(userQueryWrapper));
    CompletableFuture<?> statisticsUserData = async(userMapper::StatisticsOfDailyRegisteredUsers);
    CompletableFuture<?> dailyScreenings = async(movieShowTimeMapper::StatisticsOfDailyMovieScreenings);
    CompletableFuture<?> dailyOrders = async(movieOrderMapper::DailyOrderStatistics);
    CompletableFuture<?> dailyTxn = async(() -> movieOrderMapper.DailyTransactionAmount(
        OrderState.order_succeed.getCode(), PayState.payment_successful.getCode()));
    CompletableFuture<?> loginPlatform = async(userMapper::loginPlatformStatistics);

    CompletableFuture<Long> brandCount = async(chartMapper::countBrands);
    CompletableFuture<Long> theaterHallCount = async(chartMapper::countTheaterHalls);
    CompletableFuture<Long> tmdbMatched = async(chartMapper::countMoviesWithTmdb);
    CompletableFuture<Long> todayShowtimeCount = async(chartMapper::countTodayShowtimes);
    CompletableFuture<?> todayBrandShowtimes = async(chartMapper::todayBrandShowtimes);
    CompletableFuture<?> todayTopCinemas = async(chartMapper::todayTopCinemas);
    CompletableFuture<?> next7Days = async(chartMapper::next7DaysShowtimes);
    CompletableFuture<?> todayPrefecture = async(chartMapper::todayPrefectureShowtimes);
    CompletableFuture<?> dataQuality = async(chartMapper::movieDataQuality);
    CompletableFuture<?> kpiTrends = async(chartMapper::kpiTrends);

    CompletableFuture.allOf(
        userCount, movieCount, cinemaCount, showTimeCount,
        statisticsUserData, dailyScreenings, dailyOrders, dailyTxn, loginPlatform,
        brandCount, theaterHallCount, tmdbMatched, todayShowtimeCount,
        todayBrandShowtimes, todayTopCinemas, next7Days, todayPrefecture, dataQuality, kpiTrends
    ).join();

    ChartResponse r = new ChartResponse();
    r.setUserCount(userCount.join());
    r.setMovieCount(movieCount.join());
    r.setCinemaCount(cinemaCount.join());
    r.setShowTimeCount(showTimeCount.join());
    r.setStatisticsUserData((java.util.List) statisticsUserData.join());
    r.setStatisticsOfDailyMovieScreenings((java.util.List) dailyScreenings.join());
    r.setDailyOrderStatistics((java.util.List) dailyOrders.join());
    r.setDailyTransactionAmount((java.util.List) dailyTxn.join());
    r.setLoginPlatformStatistics((java.util.List) loginPlatform.join());

    r.setBrandCount(brandCount.join());
    r.setTheaterHallCount(theaterHallCount.join());
    r.setTmdbMatchedMovieCount(tmdbMatched.join());
    r.setTodayShowTimeCount(todayShowtimeCount.join());
    r.setTodayBrandShowtimes((java.util.List) todayBrandShowtimes.join());
    r.setTodayTopCinemas((java.util.List) todayTopCinemas.join());
    r.setNext7DaysShowtimes((java.util.List) next7Days.join());
    r.setTodayPrefectureShowtimes((java.util.List) todayPrefecture.join());
    r.setMovieDataQuality((com.example.backend.response.chart.MovieDataQuality) dataQuality.join());
    r.setKpiTrends((com.example.backend.response.chart.ChartKpiTrends) kpiTrends.join());
    return r;
  }

  private <T> CompletableFuture<T> async (Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, chartExecutor);
  }

  /**
   * 核心线程也允许空闲超时的线程池：跑完聚合后空闲 60s 自动回收，不常驻线程。
   *
   * <p>并发与拒绝策略：低核机器（如 2 核）上 {@code cpu*2} 只有 4，而一次聚合要并行提交
   * 19 个任务。若仍用 SynchronousQueue（不排队）+ 默认 AbortPolicy，第 5 个任务起就会抛
   * RejectedExecutionException 导致接口 500。这里改为：
   *  - 并发下限拉到 8（保证小机器也有足够并行度跑完聚合，上限仍 20 不超过 HikariCP 池）；
   *  - 用有界 LinkedBlockingQueue 让超出的任务排队，而不是直接拒绝；
   *  - CallerRunsPolicy 兜底：极端情况下由调用线程自己执行，绝不丢任务。
   */
  private static ExecutorService buildChartExecutor () {
    int max = Math.min(20, Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        max, max, 60L, TimeUnit.SECONDS,
        new java.util.concurrent.LinkedBlockingQueue<>(64),
        r -> {
          Thread t = new Thread(r, "chart-aggregator");
          t.setDaemon(true);
          return t;
        },
        new ThreadPoolExecutor.CallerRunsPolicy());
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  /** 容器销毁（含 DevTools 重启）时关闭线程池，避免线程泄漏耗尽原生内存。 */
  @PreDestroy
  public void shutdownExecutor () {
    chartExecutor.shutdownNow();
  }

  private static final class CachedChart {
    final ChartResponse response;
    final Instant createdAt;

    CachedChart (ChartResponse response, Instant createdAt) {
      this.response = response;
      this.createdAt = createdAt;
    }

    boolean isExpired () {
      return Duration.between(createdAt, Instant.now()).compareTo(CACHE_TTL) >= 0;
    }
  }
}
