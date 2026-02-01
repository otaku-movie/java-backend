package com.example.backend.service;

import com.example.backend.dto.SeatSelectionCancelMessage;
import com.example.backend.enumerate.RedisType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选座延迟取消消费者
 * 处理超过指定时间未支付的选座自动取消
 */
@Slf4j
@Service
public class SeatSelectionCancelConsumer {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  /**
   * 消费选座取消消息
   * 检查 Redis 中是否还存在锁定，如果存在则取消，如果不存在（已支付）则忽略
   */
  @RabbitListener(queues = "seat.selection.cancel.queue")
  public void handleSeatSelectionCancel(SeatSelectionCancelMessage message) {
    long currentTime = System.currentTimeMillis();
    // 格式化时间
    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentTimeStr = dateFormat.format(new java.util.Date(currentTime));
    
    log.warn("【⚠️ 延迟消息消费】收到选座取消消息: userId={}, movieShowTimeId={}, theaterHallId={}, 座位数={}", 
        message.getUserId(), message.getMovieShowTimeId(), message.getTheaterHallId(), 
        message.getSeatCoordinates() != null ? message.getSeatCoordinates().size() : 0);
    log.warn("【时间信息】消费时间: {} ({})", currentTime, currentTimeStr);
    log.warn("【⚠️ 注意】如果消费时间与保存时间相同或接近，说明延迟机制未生效，消息可能直接进入了处理队列！");

    try {
      // 检查 Redis 中是否还存在锁定
      String lockKey = buildLockKey(message.getMovieShowTimeId(), message.getTheaterHallId(), 
          message.getUserId(), message.getSeatCoordinates());
      
      String lockValue = redisTemplate.opsForValue().get(lockKey);
      
      // 如果 Redis 中还存在锁定，说明用户未支付，需要取消
      if (lockValue != null) {
        log.info("检测到未支付的选座，开始自动取消: lockKey={}, lockValue={}", lockKey, lockValue);
        
        // 从 Redis 中删除锁定
        redisTemplate.delete(lockKey);
        log.debug("已删除 Redis 锁定: {}", lockKey);
        
        // 从 Redis 中删除选座数据缓存（不删除数据库记录，因为只存 Redis）
        int deletedCount = 0;
        for (SeatSelectionCancelMessage.SeatCoordinate coord : message.getSeatCoordinates()) {
          if (coord.getSeatId() != null && coord.getSeatName() != null && !coord.getSeatName().isEmpty()) {
            // 直接使用消息中的 seatName，不需要查询数据库
            String seatDataKey = RedisType.seatSelectionData.getCode() + ":" + 
                message.getMovieShowTimeId() + ":" + 
                message.getTheaterHallId() + ":" + 
                coord.getSeatName() + ":" + 
                coord.getSeatId();
            Boolean deleted = redisTemplate.delete(seatDataKey);
            if (Boolean.TRUE.equals(deleted)) {
              deletedCount++;
              log.debug("已删除选座数据: seatDataKey={}", seatDataKey);
            } else {
              log.warn("选座数据不存在或已被删除: seatDataKey={}", seatDataKey);
            }
          }
        }
        
        log.info("选座自动取消成功: userId={}, movieShowTimeId={}, theaterHallId={}, 删除座位数={}/{}", 
            message.getUserId(), message.getMovieShowTimeId(), message.getTheaterHallId(), 
            deletedCount, message.getSeatCoordinates() != null ? message.getSeatCoordinates().size() : 0);
      } else {
        // 锁不存在，可能的原因：
        // 1. 用户已创建订单（在 createOrder 时调用了 unlockSeats 删除锁）
        // 2. 用户已取消选座（手动取消）
        // 3. 锁的过期时间到了（900秒）
        // 4. 其他业务逻辑删除了锁
        log.info("选座已被支付或已取消，忽略取消操作: userId={}, movieShowTimeId={}, theaterHallId={}, lockKey={} (锁不存在，可能已创建订单或已取消)", 
            message.getUserId(), message.getMovieShowTimeId(), message.getTheaterHallId(), lockKey);
      }
    } catch (Exception e) {
      log.error("处理选座取消消息失败: userId={}, movieShowTimeId={}, error={}", 
          message.getUserId(), message.getMovieShowTimeId(), e.getMessage(), e);
    }
  }

  /**
   * 构建 Redis 锁定 key
   */
  private String buildLockKey(Integer movieShowTimeId, Integer theaterHallId, 
                              Integer userId, List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    StringBuilder keyBuilder = new StringBuilder();
    keyBuilder.append("seat:selection:lock:")
        .append(movieShowTimeId)
        .append(":")
        .append(theaterHallId)
        .append(":")
        .append(userId)
        .append(":");
    
    // 将座位坐标排序后拼接，确保 key 唯一性
    seatCoordinates.stream()
        .sorted((a, b) -> {
          int xCompare = a.getX().compareTo(b.getX());
          return xCompare != 0 ? xCompare : a.getY().compareTo(b.getY());
        })
        .forEach(coord -> keyBuilder.append(coord.getX()).append(",").append(coord.getY()).append(";"));
    
    return keyBuilder.toString();
  }
}
