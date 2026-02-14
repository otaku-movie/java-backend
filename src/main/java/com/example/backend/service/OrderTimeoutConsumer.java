package com.example.backend.service;

import com.example.backend.dto.OrderTimeoutMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单超时消费者（RabbitMQ）
 * 消费延迟队列到期后的订单超时消息，与 {@link MovieOrderService#processOrderTimeout} 统一处理：
 * 订单置为超时、清理 Redis locked 座位、将 select_seat 表中该订单座位置为可用。
 */
@Slf4j
@Service
public class OrderTimeoutConsumer {

  @Autowired
  private MovieOrderService movieOrderService;

  @RabbitListener(queues = "order.timeout.queue")
  @Transactional(rollbackFor = Exception.class)
  public void handleOrderTimeout(OrderTimeoutMessage message) {
    log.info("【订单超时消息消费】收到订单超时消息: orderNumber={}, orderId={}",
        message.getOrderNumber(), message.getOrderId());
    try {
      boolean processed = movieOrderService.processOrderTimeout(message.getOrderId());
      if (!processed) {
        log.debug("订单超时消息未执行处理（订单不存在或状态已变更）: orderId={}", message.getOrderId());
      }
    } catch (Exception e) {
      log.error("处理订单超时消息失败: orderNumber={}, orderId={}, error={}",
          message.getOrderNumber(), message.getOrderId(), e.getMessage(), e);
      throw e;
    }
  }
}
