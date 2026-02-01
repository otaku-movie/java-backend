package com.example.backend.service;

import com.example.backend.dto.OrderTimeoutMessage;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.constants.MessageKeys;
import com.example.backend.exception.BusinessException;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单超时消费者
 * 处理超过支付时间的订单自动超时
 */
@Slf4j
@Service
public class OrderTimeoutConsumer {

  @Autowired
  private MovieOrderMapper movieOrderMapper;
  
  @Autowired
  private SelectSeatMapper selectSeatMapper;
  
  @Autowired
  private SelectSeatService selectSeatService;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  /**
   * 消费订单超时消息
   * 检查订单状态，如果未支付则设置为超时，并删除 Redis 中的 locked 状态座位
   */
  @RabbitListener(queues = "order.timeout.queue")
  @Transactional
  public void handleOrderTimeout(OrderTimeoutMessage message) {
    long currentTime = System.currentTimeMillis();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentTimeStr = dateFormat.format(new Date(currentTime));
    
    log.warn("【订单超时消息消费】收到订单超时消息: orderNumber={}, orderId={}", 
        message.getOrderNumber(), message.getOrderId());
    log.warn("【时间信息】消费时间: {} ({})", currentTime, currentTimeStr);

    try {
      // 1. 查询订单
      MovieOrder order = movieOrderMapper.selectById(message.getOrderId());
      if (order == null) {
        log.warn("订单不存在，忽略超时处理: orderNumber={}, orderId={}", 
            message.getOrderNumber(), message.getOrderId());
        return;
      }
      
      // 2. 检查订单状态：只有已创建且未支付的订单才需要超时处理
      if (order.getOrderState() == null || 
          !order.getOrderState().equals(OrderState.order_created.getCode())) {
        log.info("订单状态已变更，忽略超时处理: orderNumber={}, orderId={}, orderState={}", 
            message.getOrderNumber(), message.getOrderId(), order.getOrderState());
        return;
      }
      
      // 3. 更新订单状态为超时
      order.setOrderState(OrderState.order_timeout.getCode());
      movieOrderMapper.updateById(order);
      log.info("订单已设置为超时: orderNumber={}, orderId={}", 
          message.getOrderNumber(), message.getOrderId());
      
      // 4. 从 Redis 删除 locked 状态的座位（让用户重新选座）
      Integer theaterHallId = null;
      if (order.getMovieShowTimeId() != null) {
        MovieShowTime st = movieShowTimeMapper.selectById(order.getMovieShowTimeId());
        theaterHallId = st != null ? st.getTheaterHallId() : null;
      }
      if (order.getMovieShowTimeId() != null && theaterHallId != null) {
        List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            order.getMovieShowTimeId(), theaterHallId, message.getOrderId());
        
        if (!lockedSeats.isEmpty()) {
          Map<Integer, String> lockedSeatIdToNameMap = lockedSeats.stream()
              .filter(seat -> seat.getSeatId() != null && seat.getSeatName() != null)
              .collect(Collectors.toMap(
                  SelectSeat::getSeatId,
                  SelectSeat::getSeatName,
                  (existing, replacement) -> existing));
          
          if (!lockedSeatIdToNameMap.isEmpty()) {
            SelectSeat firstSeat = lockedSeats.get(0);
            selectSeatService.deleteSeatsFromRedis(
                firstSeat.getMovieShowTimeId(),
                firstSeat.getTheaterHallId(),
                lockedSeatIdToNameMap
            );
            log.info("订单超时，已从 Redis 删除 locked 状态座位: orderNumber={}, orderId={}, seatCount={}", 
                message.getOrderNumber(), message.getOrderId(), lockedSeatIdToNameMap.size());
          }
        }
      }
      
      log.info("订单超时处理完成: orderNumber={}, orderId={}", 
          message.getOrderNumber(), message.getOrderId());
    } catch (Exception e) {
      log.error("处理订单超时消息失败: orderNumber={}, orderId={}, error={}", 
          message.getOrderNumber(), message.getOrderId(), e.getMessage(), e);
    }
  }
}
