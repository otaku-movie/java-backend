package com.example.backend.controller.scheduledTasks;

import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.service.MovieOrderService;
import com.example.backend.service.MovieService;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableScheduling
@Slf4j
@RestController
public class ScheduledTasksController {

  @Autowired
  private MovieService movieService;

  @Autowired
  private MovieShowTimeService movieShowTimeService;

  @Autowired
  private MovieOrderService movieOrderService;

  @Value("${order.payment-timeout:900}")
  private int orderPaymentTimeoutSeconds;

  /**
   * 定时任务：更新电影上映日期状态。
   * 根据电影的 startDate/endDate 将状态更新为「上映中」或「已结束」，每天 00:00 执行。
   */
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_STATE)
  @Scheduled(cron = "0 0 0 * * ?")
  public RestBean<Object> updateMovieState() {
    log.info("Updating movie state...");
    movieService.updateMovieReleaseState();
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Success.GENERAL));
  }

  /**
   * 定时任务：更新场次放映状态。
   * 根据场次 start_time/end_time 将状态更新为「未上映」「上映中」或「已结束」，每分钟执行。
   */
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_SCREENING_STATE)
  @Scheduled(cron = "0 * * * * ?")
  public RestBean<Object> updateMovieScreeningState() {
    log.info("Updating movie screening state...");
    movieShowTimeService.updateScreeningState();
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Success.GENERAL));
  }

  /**
   * 定时任务：更新场次定时公开与可购票状态。
   * 到达 publish_at 时自动将 open 置为 true；根据 sale_open_at 维护 can_sale。每分钟执行。
   */
  @PostMapping(ApiPaths.Scheduled.UPDATE_SHOWTIME_PUBLISH_STATE)
  @Scheduled(cron = "0 * * * * ?")
  public RestBean<Object> updateShowTimePublishState() {
    log.info("Updating showtime publish/sale state...");
    movieShowTimeService.updatePublishAndCanSaleState();
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Success.GENERAL));
  }

  /**
   * 定时任务：选座状态（预留）。
   * 当前无逻辑，选座超时释放由订单超时任务统一处理。每分钟执行。
   */
  @Scheduled(cron = "0 * * * * ?")
  public void updateMovieSeatSelectionState() {
    log.debug("updateMovieSeatSelectionState: placeholder, no-op.");
  }

  /**
   * 定时任务：订单超时兜底。
   * 扫描仍为「已创建」且已超过支付超时时间的订单，置为超时并释放 Redis 选座及 DB 选座状态；与 RabbitMQ 超时消费者互补。每分钟执行。
   */
  @PostMapping(ApiPaths.Scheduled.UPDATE_MOVIE_ORDER_STATE)
  @Scheduled(cron = "0 * * * * ?")
  public void updateMovieOrderState() {
    log.debug("Updating movie order state (fallback for timeout)...");
    int processed = movieOrderService.processExpiredOrdersFallback(orderPaymentTimeoutSeconds);
    if (processed > 0) {
      log.info("订单超时兜底任务处理数量: {}", processed);
    }
  }
}
