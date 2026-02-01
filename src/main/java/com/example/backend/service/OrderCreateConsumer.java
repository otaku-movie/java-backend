package com.example.backend.service;

import com.example.backend.config.RabbitMQConfig;
import com.example.backend.dto.OrderCreateMessage;
import com.example.backend.dto.OrderTimeoutMessage;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.enumerate.SeatState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.constants.MessageKeys;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.MovieOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单创建消费者
 * 通过 RabbitMQ 异步创建订单和 locked 状态的选座记录
 */
@Slf4j
@Service
public class OrderCreateConsumer {

  @Autowired
  private MovieOrderMapper movieOrderMapper;

  @Autowired
  private SelectSeatService selectSeatService;
  
  @Autowired
  private RabbitTemplate rabbitTemplate;
  
  @Value("${order.payment-timeout:900}")
  private int paymentTimeoutSeconds;

  /**
   * 消费订单创建消息
   * 创建订单并更新座位状态为 locked（存数据库）
   */
  @RabbitListener(queues = "order.create.queue")
  @Transactional
  public void handleOrderCreate(OrderCreateMessage message) {
    log.info("收到订单创建消息: orderNumber={}, userId={}, movieShowTimeId={}", 
        message.getOrderNumber(), message.getUserId(), message.getMovieShowTimeId());

    try {
      // 1. 创建订单
      MovieOrder movieOrder = new MovieOrder();
      movieOrder.setMovieShowTimeId(message.getMovieShowTimeId());
      movieOrder.setUserId(message.getUserId());
      movieOrder.setOrderNumber(message.getOrderNumber());
      movieOrder.setOrderState(OrderState.order_created.getCode());
      movieOrder.setOrderTotal(message.getOrderTotal());
      movieOrder.setPayState(PayState.waiting_for_payment.getCode());
      
      movieOrderMapper.insert(movieOrder);
      log.info("订单创建成功: orderId={}, orderNumber={}", movieOrder.getId(), message.getOrderNumber());

      // 2. locked 状态不存数据库，只更新 Redis（只有 sold 状态存数据库）
      // 注意：locked 状态只存在 Redis 中，不保存到数据库
      // 按 seatId 去重，防止重复座位
      List<OrderCreateMessage.SeatInfo> uniqueSeats = message.getSeats().stream()
          .filter(s -> s.getSeatId() != null)
          .collect(Collectors.toMap(OrderCreateMessage.SeatInfo::getSeatId, s -> s, (a, b) -> a, LinkedHashMap::new))
          .values().stream().collect(Collectors.toList());
      List<SelectSeat> lockedSeats = uniqueSeats.stream().map(seatInfo -> {
        SelectSeat seat = new SelectSeat();
        seat.setUserId(message.getUserId());
        seat.setMovieShowTimeId(message.getMovieShowTimeId());
        seat.setTheaterHallId(message.getTheaterHallId());
        seat.setX(seatInfo.getX());
        seat.setY(seatInfo.getY());
        seat.setSeatId(seatInfo.getSeatId());
        seat.setSeatName(seatInfo.getSeatName());
        seat.setMovieTicketTypeId(seatInfo.getMovieTicketTypeId());
        seat.setMovieOrderId(movieOrder.getId());
        seat.setSelectSeatState(SeatState.locked.getCode());
        return seat;
      }).collect(Collectors.toList());

      // 3. 更新 Redis 中的座位信息，添加 movieOrderId（locked 状态只存 Redis）
      for (SelectSeat seat : lockedSeats) {
        selectSeatService.saveSeatToRedis(seat);
      }
      log.info("locked 状态选座记录已更新到 Redis: orderId={}, seatCount={}", movieOrder.getId(), lockedSeats.size());

      // 3.1 同时写入 select_seat 表，便于取消/超时后订单详情仍能查到当时的座位
      if (!lockedSeats.isEmpty()) {
        // 幂等：先删除该订单下已有座位，防止消息重复消费或重复插入导致座位重复
        selectSeatService.remove(new LambdaQueryWrapper<SelectSeat>()
            .eq(SelectSeat::getMovieOrderId, movieOrder.getId()));
        // 再按 seat_id 去重一次，避免同一批次内重复
        List<SelectSeat> deduped = lockedSeats.stream()
            .collect(Collectors.toMap(SelectSeat::getSeatId, s -> s, (a, b) -> a, LinkedHashMap::new))
            .values().stream()
            .collect(Collectors.toList());
        selectSeatService.saveBatch(deduped);
        log.info("locked 状态选座记录已写入 DB: orderId={}, seatCount={}", movieOrder.getId(), deduped.size());
      }

      // 4. 发送订单超时延迟消息到 RabbitMQ（使用 TTL + DLX 方案）
      OrderTimeoutMessage timeoutMessage = new OrderTimeoutMessage();
      timeoutMessage.setOrderNumber(message.getOrderNumber());
      timeoutMessage.setOrderId(movieOrder.getId());
      
      long ttlMillis = paymentTimeoutSeconds * 1000L;
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.ORDER_TIMEOUT_EXCHANGE,
          RabbitMQConfig.ORDER_TIMEOUT_ROUTING_KEY,
          timeoutMessage,
          msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(ttlMillis));
            log.info("订单超时延迟消息已发送: orderNumber={}, orderId={}, TTL={}ms ({}秒)", 
                message.getOrderNumber(), movieOrder.getId(), ttlMillis, paymentTimeoutSeconds);
            return msg;
          });

    } catch (Exception e) {
      log.error("处理订单创建消息失败: orderNumber={}, error={}", message.getOrderNumber(), e.getMessage(), e);
      throw new BusinessException(ResponseCode.ORDER_CREATE_FAILED, MessageKeys.Error.ORDER_CREATE_FAILED, e);
    }
  }
}
