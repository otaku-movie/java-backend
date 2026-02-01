package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.config.RabbitMQConfig;
import com.example.backend.dto.SeatSelectionCancelMessage;
import com.alibaba.fastjson2.JSON;
import com.example.backend.entity.*;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.SeatResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.example.backend.exception.BusinessException;
import com.example.backend.constants.MessageKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author last order
 * @description 针对表【api】的数据库操作Service
 * @createDate 2024-05-24 17:37:24
 */

@Slf4j
@Service
public class SelectSeatService extends ServiceImpl<SelectSeatMapper, SelectSeat> {
  @Autowired
  SeatMapper seatMapper;
  @Autowired
  SeatAreaMapper seatAreaMapper;
  @Autowired
  SeatAisleMapper seatAisleMapper;
  @Autowired
  SeatAreaService seatAreaService;
  @Autowired
  SeatAisleService seatAisleService;
  @Autowired
  CinemaMapper cinemaMapper;
  @Autowired
  TheaterHallMapper theaterHallMapper;

  @Autowired
  SelectSeatMapper selectSeatMapper;
  @Autowired
  SeatService seatService;
  @Autowired
  MovieOrderMapper movieOrderMapper;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Value("${seat-selection.auto-cancel-timeout:900}")
  private int autoCancelTimeoutSeconds;

  /**
   * 获取座位列表（优化版：优先从 Redis 读取选座信息）
   * 
   * @param theaterHallId   影厅ID
   * @param movieShowTimeId 场次ID
   * @return 座位详情响应
   */
  public Object selectSeatList(Integer theaterHallId, Integer movieShowTimeId) {
    Integer userId = StpUtil.getLoginIdAsInt();
    
    // 1. 从数据库查询所有座位的基础信息（seat 表，不包含选座状态）
    List<SeatListResponse> allSeats = seatMapper.seatList(theaterHallId);
    if (allSeats == null || allSeats.isEmpty()) {
      // 如果没有座位，返回空结果
      SeatDetailResponse seatDetailResponse = new SeatDetailResponse();
      TheaterHall theaterHall = theaterHallMapper.selectById(theaterHallId);
      if (theaterHall != null) {
        Cinema cinema = cinemaMapper.selectById(theaterHall.getCinemaId());
        seatDetailResponse.setMaxSelectSeatCount(cinema != null ? cinema.getMaxSelectSeatCount() : 0);
        seatDetailResponse.setTotalSeatCount(theaterHallMapper.getSeatCount(theaterHallId));
      }
      seatDetailResponse.setAisle(seatService.getSeatAisle(theaterHallId));
      seatDetailResponse.setSeat(new ArrayList<>());
      seatDetailResponse.setArea(seatService.getSeatArea(theaterHallId));
      return seatDetailResponse;
    }

    // 2. 从 Redis 读取该场次该影厅的所有选座信息（selected、locked、sold 状态）
    Map<Integer, SelectSeat> redisSeatMap = new HashMap<>();
    String pattern = RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    
    if (keys != null && !keys.isEmpty()) {
      for (String key : keys) {
        String seatJson = redisTemplate.opsForValue().get(key);
        if (seatJson != null) {
          try {
            SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
            if (seat != null && seat.getSeatId() != null && 
                (seat.getSelectSeatState() == SeatState.selected.getCode() || 
                 seat.getSelectSeatState() == SeatState.locked.getCode() ||
                 seat.getSelectSeatState() == SeatState.sold.getCode())) {
              redisSeatMap.put(seat.getSeatId(), seat);
            }
          } catch (Exception ignore) {
            // JSON 解析异常忽略
          }
        }
      }
    }

    // 3. 如果 Redis 没有数据，从数据库读取 sold 状态并缓存（只有 sold 状态存数据库）
    if (redisSeatMap.isEmpty()) {
      // 从数据库查询选座信息（只查询 sold 状态，selected 和 locked 不存数据库）
      QueryWrapper<SelectSeat> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("movie_show_time_id", movieShowTimeId)
          .eq("theater_hall_id", theaterHallId)
          .eq("deleted", 0)
          .eq("select_seat_state", SeatState.sold.getCode());
      List<SelectSeat> dbSeats = selectSeatMapper.selectList(queryWrapper);
      
      // 缓存到 Redis（只缓存 sold 状态）
      for (SelectSeat seat : dbSeats) {
        if (seat.getSeatId() != null && seat.getSeatName() != null) {
          redisSeatMap.put(seat.getSeatId(), seat);
          saveSeatToRedis(seat);
        }
      }
    }

    // 4. 合并选座状态到座位列表
    for (SeatListResponse seat : allSeats) {
      // 从 Redis 中获取选座信息（包含 selected、locked、sold 三种状态）
      SelectSeat selectedSeat = redisSeatMap.get(seat.getId());
      if (selectedSeat != null) {
        Integer seatState = selectedSeat.getSelectSeatState();
        
        // 对于 selected 状态，只返回当前用户是否选择
        if (seatState != null && seatState.intValue() == SeatState.selected.getCode()) {
          boolean isCurrentUserSelected = selectedSeat.getUserId() != null &&
              selectedSeat.getUserId().equals(userId);
          seat.setSelected(isCurrentUserSelected);
          // selected 状态只作为前端标记，不返回具体状态值（前端通过 selected 字段判断）
          seat.setSelectSeatState(SeatState.available.getCode()); // 前端通过 selected 字段判断
        } else if (seatState != null && seatState.intValue() == SeatState.locked.getCode()) {
          // locked 状态：只作为前端标记，不返回用户信息
          seat.setSelected(false);
          seat.setSelectSeatState(SeatState.locked.getCode());
        } else if (seatState != null && seatState.intValue() == SeatState.sold.getCode()) {
          // sold 状态：保存到数据库，返回完整状态
          seat.setSelected(false);
          seat.setSelectSeatState(SeatState.sold.getCode());
        } else {
          seat.setSelected(false);
          seat.setSelectSeatState(SeatState.available.getCode());
        }
      } else {
        // 未被选中
        seat.setSelected(false);
        seat.setSelectSeatState(SeatState.available.getCode());
      }
    }

    // 6. 按行分组
    List<SeatResponse> result = new ArrayList<>();
    Map<Integer, List<SeatListResponse>> map = allSeats.stream().collect(
        Collectors.groupingBy(SeatListResponse::getX));

    map.forEach((row, seats) -> {
      SeatResponse modal = new SeatResponse();
      modal.setRowAxis(row);
      modal.setChildren(seats);
      if (!seats.isEmpty() && seats.get(0).getRowName() != null) {
        modal.setRowName(seats.get(0).getRowName());
      }
      result.add(modal);
    });

    // 6. 查询影院最大选座数量
    TheaterHall theaterHall = theaterHallMapper.selectById(theaterHallId);
    Cinema cinema = theaterHall != null ? cinemaMapper.selectById(theaterHall.getCinemaId()) : null;

    // 7. 查询影厅座位数
    Integer totalSeatCount = theaterHallMapper.getSeatCount(theaterHallId);

    SeatDetailResponse seatDetailResponse = new SeatDetailResponse();
    seatDetailResponse.setMaxSelectSeatCount(cinema != null ? cinema.getMaxSelectSeatCount() : 0);
    seatDetailResponse.setTotalSeatCount(totalSeatCount != null ? totalSeatCount : 0);
    seatDetailResponse.setAisle(seatService.getSeatAisle(theaterHallId));
    seatDetailResponse.setSeat(result);
    seatDetailResponse.setArea(seatService.getSeatArea(theaterHallId));

    // 检测当前用户在本场次是否有未支付的锁定订单
    SelectSeat userLockedSeat = redisSeatMap.values().stream()
        .filter(s -> s.getSelectSeatState() != null && s.getSelectSeatState().intValue() == SeatState.locked.getCode())
        .filter(s -> s.getUserId() != null && s.getUserId().equals(userId))
        .filter(s -> s.getMovieOrderId() != null)
        .findFirst()
        .orElse(null);
    if (userLockedSeat != null) {
      MovieOrder order = movieOrderMapper.selectById(userLockedSeat.getMovieOrderId());
      if (order != null && order.getOrderNumber() != null) {
        seatDetailResponse.setHasLockedOrder(true);
        seatDetailResponse.setOrderNumber(order.getOrderNumber());
      }
    }

    return seatDetailResponse;
  }

  /**
   * 锁定选座（存储到 Redis）
   * 
   * @param userId          用户ID
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatCoordinates 座位坐标列表
   * @return Redis key
   */
  public String lockSeats(Integer userId, Integer movieShowTimeId, Integer theaterHallId,
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    String lockKey = buildLockKey(movieShowTimeId, theaterHallId, userId, SeatState.selected.getCode(),
        seatCoordinates);

    // 将座位信息序列化为 JSON 存储到 Redis，设置过期时间为900秒 (15分钟)
    redisTemplate.opsForValue().set(lockKey, String.valueOf(SeatState.locked.getCode()), autoCancelTimeoutSeconds,
        TimeUnit.SECONDS);

    return lockKey;
  }

  /**
   * 检查座位是否被锁定（在 Redis 中）
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatCoordinates 座位坐标列表
   * @return true 如果座位被锁定
   */
  public boolean isSeatsLocked(Integer movieShowTimeId, Integer theaterHallId,
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    // 检查是否有任何用户锁定了这些座位
    // 这里简化处理，实际应该检查所有可能的用户锁定
    // 可以通过 Redis 的 SET 数据结构存储每个座位的锁定信息
    for (SeatSelectionCancelMessage.SeatCoordinate coord : seatCoordinates) {
      String seatKey = buildSeatLockKey(movieShowTimeId, theaterHallId, coord.getX(), coord.getY());
      String lockValue = redisTemplate.opsForValue().get(seatKey);
      if (lockValue != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * 锁定单个座位（用于并发控制）
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param x               座位X坐标
   * @param y               座位Y坐标
   * @param userId          用户ID
   * @return true 如果锁定成功
   */
  public boolean lockSingleSeat(Integer movieShowTimeId, Integer theaterHallId,
      Integer x, Integer y, Integer userId) {
    String seatKey = buildSeatLockKey(movieShowTimeId, theaterHallId, x, y);

    // 使用 SETNX 实现分布式锁
    Boolean success = redisTemplate.opsForValue().setIfAbsent(seatKey, String.valueOf(userId),
        autoCancelTimeoutSeconds, TimeUnit.SECONDS);

    return Boolean.TRUE.equals(success);
  }

  /**
   * 释放单个座位锁定
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param x               座位X坐标
   * @param y               座位Y坐标
   */
  public void unlockSingleSeat(Integer movieShowTimeId, Integer theaterHallId, Integer x, Integer y) {
    String seatKey = buildSeatLockKey(movieShowTimeId, theaterHallId, x, y);
    redisTemplate.delete(seatKey);
  }

  /**
   * 发送延迟取消消息到 RabbitMQ（使用 TTL + DLX 方案）
   * 
   * 消息处理流程：
   * 1. 消息发送到延迟队列（seat.selection.cancel.delay.queue），设置 TTL（默认15分钟）
   * 2. TTL 过期后，消息自动转发到死信交换机（seat.selection.cancel.dlx.exchange）
   * 3. 死信交换机路由到处理队列（seat.selection.cancel.queue）
   * 4. SeatSelectionCancelConsumer.handleSeatSelectionCancel 消费消息
   * 5. 消费者检查 Redis 中是否还存在锁定，如果存在则删除（说明用户未支付）
   * 
   * 注意：这是异步处理，消息发送后方法立即返回，实际取消操作在 TTL 过期后执行
   *
   * @param userId          用户ID
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatCoordinates 座位坐标列表
   */
  public void sendCancelMessage(Integer userId, Integer movieShowTimeId, Integer theaterHallId,
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    SeatSelectionCancelMessage message = new SeatSelectionCancelMessage();
    message.setUserId(userId);
    message.setMovieShowTimeId(movieShowTimeId);
    message.setTheaterHallId(theaterHallId);
    message.setSeatCoordinates(seatCoordinates);

    try {
      // 发送延迟消息（使用 TTL + DLX 方案）
      // 消息发送到延迟队列，设置 TTL，过期后自动转发到死信交换机，再路由到处理队列
      long ttlMillis = autoCancelTimeoutSeconds * 1000L;
      
      if (ttlMillis <= 0) {
        log.error("TTL 配置错误: {}ms，消息将立即过期！", ttlMillis);
        throw new BusinessException(ResponseCode.PARAMETER_VALIDATION_ERROR, MessageKeys.Error.TTL_INVALID, ttlMillis);
      }
      
      // 记录保存时间（当前时间）
      long saveTime = System.currentTimeMillis();
      // 计算过期时间（保存时间 + TTL）
      long expireTime = saveTime + ttlMillis;
      
      // 格式化时间
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String saveTimeStr = dateFormat.format(new java.util.Date(saveTime));
      String expireTimeStr = dateFormat.format(new java.util.Date(expireTime));
      
      log.info("发送延迟取消消息: userId={}, movieShowTimeId={}, theaterHallId={}, TTL={}ms ({}秒), 座位数={}, 交换机={}, 路由键={}",
          userId, movieShowTimeId, theaterHallId, ttlMillis, autoCancelTimeoutSeconds, seatCoordinates.size(),
          RabbitMQConfig.SEAT_SELECTION_CANCEL_EXCHANGE, RabbitMQConfig.SEAT_SELECTION_CANCEL_ROUTING_KEY);
      log.info("【时间信息】保存时间: {} ({}), 过期时间: {} ({}), 预计 {} 秒后过期",
          saveTime, saveTimeStr,
          expireTime, expireTimeStr,
          autoCancelTimeoutSeconds);
      
      // 使用 MessagePostProcessor 设置消息的 TTL
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.SEAT_SELECTION_CANCEL_EXCHANGE,
          RabbitMQConfig.SEAT_SELECTION_CANCEL_ROUTING_KEY,
          message,
          msg -> {
            // 设置消息的 TTL（毫秒），消息在延迟队列中等待指定时间后过期
            // 过期后的消息会被发送到死信交换机（DLX），再路由到实际处理队列
            // 注意：expiration 必须是字符串格式的毫秒数
            String expiration = String.valueOf(ttlMillis);
            msg.getMessageProperties().setExpiration(expiration);
      log.info("【消息发送详情】交换机: {}, 路由键: {}, TTL: {}ms, expiration属性: {}, 消息ID: {}", 
          RabbitMQConfig.SEAT_SELECTION_CANCEL_EXCHANGE,
          RabbitMQConfig.SEAT_SELECTION_CANCEL_ROUTING_KEY,
          ttlMillis, expiration, msg.getMessageProperties().getMessageId());
      log.info("【预期行为】消息应该进入延迟队列: {}, 等待 {} 秒后过期，然后转发到处理队列: {}", 
          RabbitMQConfig.SEAT_SELECTION_CANCEL_DELAY_QUEUE,
          autoCancelTimeoutSeconds,
          RabbitMQConfig.SEAT_SELECTION_CANCEL_QUEUE);
      log.warn("【⚠️ 重要提示】如果消息立即被消费，请检查：");
      log.warn("  1. RabbitMQ 管理界面中是否存在旧的绑定（处理队列直接绑定到普通交换机）");
      log.warn("  2. 延迟队列 {} 是否正确创建并配置了 DLX", RabbitMQConfig.SEAT_SELECTION_CANCEL_DELAY_QUEUE);
      log.warn("  3. 消息的 expiration 属性是否正确设置（当前值: {}ms）", ttlMillis);
            return msg;
          });
      
      log.info("延迟取消消息已发送成功: userId={}, movieShowTimeId={}, 消息将在 {} 秒后处理（预计在 {} 秒后消费）",
          userId, movieShowTimeId, autoCancelTimeoutSeconds, autoCancelTimeoutSeconds);
    } catch (Exception e) {
      // 消息发送失败时记录错误日志，但不抛出异常（避免影响主流程）
      // 如果消息发送失败，Redis 中的锁定会在 TTL 过期后自动删除，不会造成死锁
      log.error("发送延迟取消消息失败: userId={}, movieShowTimeId={}, error={}",
          userId, movieShowTimeId, e.getMessage(), e);
      // 如果需要更严格的错误处理，可以抛出异常或使用重试机制
    }
  }

  /**
   * 释放选座锁定（支付成功后调用）
   * 
   * @param userId          用户ID
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatCoordinates 座位坐标列表
   */
  public void unlockSeats(Integer userId, Integer movieShowTimeId, Integer theaterHallId,
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    // 删除整体锁定 key
    String lockKey = buildLockKey(movieShowTimeId, theaterHallId, userId, SeatState.selected.getCode(),
        seatCoordinates);
    redisTemplate.delete(lockKey);

    // 删除单个座位锁定
    for (SeatSelectionCancelMessage.SeatCoordinate coord : seatCoordinates) {
      unlockSingleSeat(movieShowTimeId, theaterHallId, coord.getX(), coord.getY());
    }
  }

  /**
   * 构建 Redis 锁定 key（整体锁定）
   */
  private String buildLockKey(
      Integer movieShowTimeId,
      Integer theaterHallId,
      Integer userId,
      Integer selectSeatState,
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates) {
    StringBuilder keyBuilder = new StringBuilder();
    keyBuilder.append(RedisType.seatSelectionLock.getCode())
        .append(":")
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

  /**
   * 构建单个座位的锁定 key
   */
  private String buildSeatLockKey(Integer movieShowTimeId, Integer theaterHallId, Integer x, Integer y) {
    return RedisType.seatSelectionLock.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":" + x + "," + y;
  }

  /**
   * 构建单个座位的选座数据 key
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatNumber      座位编号（seatName），必填
   * @param seatId          座位ID，必填
   * @return Redis key
   */
  private String buildSeatDataKey(Integer movieShowTimeId, Integer theaterHallId,
      String seatNumber, Integer seatId) {
    return RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":" + seatNumber + ":" + seatId;
  }

  /**
   * 从 Redis 读取选座信息
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatIdToNameMap 座位ID到座位名称的映射 (seatId -> seatName)
   * @return 选座信息列表，如果 Redis 中没有则返回空列表
   */
  public List<SelectSeat> getSeatsFromRedis(
      Integer movieShowTimeId,
      Integer theaterHallId,
      Map<Integer, String> seatIdToNameMap) {
    List<SelectSeat> seats = new ArrayList<>();

    for (Map.Entry<Integer, String> entry : seatIdToNameMap.entrySet()) {
      Integer seatId = entry.getKey();
      String seatName = entry.getValue();

      if (seatId == null || seatName == null || seatName.isEmpty()) {
        continue;
      }

      String seatKey = buildSeatDataKey(movieShowTimeId, theaterHallId, seatName, seatId);
      String seatJson = redisTemplate.opsForValue().get(seatKey);

      if (seatJson != null) {
        try {
          SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
          seats.add(seat);
        } catch (Exception e) {
          // JSON 解析失败，忽略这个座位
        }
      }
    }

    return seats;
  }

  /**
   * 将选座信息写入 Redis
   * 
   * @param seat 选座信息（必须包含 seatId）
   */
  public void saveSeatToRedis(SelectSeat seat) {
    if (seat.getSeatId() == null) {
      throw new BusinessException(ResponseCode.SEAT_NOT_FOUND, MessageKeys.Error.SEAT_ID_REQUIRED);
    }
    if (seat.getSeatName() == null || seat.getSeatName().isEmpty()) {
      throw new BusinessException(ResponseCode.SEAT_NOT_FOUND, MessageKeys.Error.SEAT_NAME_REQUIRED, seat.getSeatId());
    }

    // 直接使用 SelectSeat 中的 seatName，不需要查询数据库
    String seatKey = buildSeatDataKey(seat.getMovieShowTimeId(), seat.getTheaterHallId(),
        seat.getSeatName(), seat.getSeatId());
    String seatJson = JSON.toJSONString(seat);

    // 设置过期时间，与自动取消时间一致
    redisTemplate.opsForValue().set(seatKey, seatJson, autoCancelTimeoutSeconds, TimeUnit.SECONDS);
  }

  /**
   * 从 Redis 删除选座信息
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatIdToNameMap 座位ID到座位名称的映射 (seatId -> seatName)
   */
  public void deleteSeatsFromRedis(Integer movieShowTimeId, Integer theaterHallId,
      Map<Integer, String> seatIdToNameMap) {
    for (Map.Entry<Integer, String> entry : seatIdToNameMap.entrySet()) {
      Integer seatId = entry.getKey();
      String seatName = entry.getValue();

      if (seatId == null || seatName == null || seatName.isEmpty()) {
        continue;
      }

      String seatKey = buildSeatDataKey(movieShowTimeId, theaterHallId, seatName, seatId);
      redisTemplate.delete(seatKey);
    }
  }

  /**
   * 直接从 Redis 获取当前用户在某场次某影厅下已选择的座位
   * 不依赖数据库的选座记录
   */
  public List<SelectSeat> getUserSelectedSeatsFromRedis(
      Integer movieShowTimeId,
      Integer theaterHallId,
      Integer userId) {

    List<SelectSeat> result = new ArrayList<>();
    if (movieShowTimeId == null || theaterHallId == null || userId == null) {
      return result;
    }

    // 按场次 + 影厅前缀扫描所有选座数据
    String pattern = RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":*";

    Set<String> keys = redisTemplate.keys(pattern);
    if (keys == null || keys.isEmpty()) {
      return result;
    }

    for (String key : keys) {
      String seatJson = redisTemplate.opsForValue().get(key);
      if (seatJson == null) {
        continue;
      }
      try {
        SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
        if (seat == null) {
          continue;
        }
        if (seat.getUserId() == null || !userId.equals(seat.getUserId())) {
          continue;
        }
        if (seat.getSelectSeatState() == null ||
            seat.getSelectSeatState().intValue() != SeatState.selected.getCode()) {
          continue;
        }
        result.add(seat);
      } catch (Exception ignore) {
        // JSON 解析异常忽略
      }
    }

    return result;
  }

  /**
   * 从 Redis 获取当前用户在某场次某影厅下已锁定的座位（locked 状态）
   * 用于检查是否已创建订单
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param userId          用户ID
   * @param seatIds         要检查的座位ID列表
   * @return 已锁定的座位列表（如果所有座位都已锁定，说明已创建订单）
   */
  public List<SelectSeat> getUserLockedSeatsFromRedis(
      Integer movieShowTimeId,
      Integer theaterHallId,
      Integer userId,
      List<Integer> seatIds) {

    List<SelectSeat> result = new ArrayList<>();
    if (movieShowTimeId == null || theaterHallId == null || userId == null || seatIds == null || seatIds.isEmpty()) {
      return result;
    }

    // 按场次 + 影厅前缀扫描所有选座数据
    String pattern = RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":*";

    Set<String> keys = redisTemplate.keys(pattern);
    if (keys == null || keys.isEmpty()) {
      return result;
    }

    for (String key : keys) {
      String seatJson = redisTemplate.opsForValue().get(key);
      if (seatJson == null) {
        continue;
      }
      try {
        SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
        if (seat == null || seat.getSeatId() == null) {
          continue;
        }
        // 检查是否是目标座位之一
        if (!seatIds.contains(seat.getSeatId())) {
          continue;
        }
        // 检查是否是当前用户
        if (seat.getUserId() == null || !userId.equals(seat.getUserId())) {
          continue;
        }
        // 检查状态是否为 locked
        if (seat.getSelectSeatState() != null &&
            seat.getSelectSeatState().intValue() == SeatState.locked.getCode()) {
          result.add(seat);
        }
      } catch (Exception ignore) {
        // JSON 解析异常忽略
      }
    }

    return result;
  }

  /**
   * 从 Redis 获取指定订单ID的 locked 状态座位
   * 用于支付成功后转换为 sold 状态
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param movieOrderId    订单ID
   * @return locked 状态的座位列表
   */
  public List<SelectSeat> getLockedSeatsByOrderIdFromRedis(
      Integer movieShowTimeId,
      Integer theaterHallId,
      Integer movieOrderId) {
    List<SelectSeat> result = new ArrayList<>();
    if (movieShowTimeId == null || theaterHallId == null || movieOrderId == null) {
      return result;
    }

    // 按场次 + 影厅前缀扫描所有选座数据
    String pattern = RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":*";

    Set<String> keys = redisTemplate.keys(pattern);
    if (keys == null || keys.isEmpty()) {
      return result;
    }

    for (String key : keys) {
      String seatJson = redisTemplate.opsForValue().get(key);
      if (seatJson == null) {
        continue;
      }
      try {
        SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
        if (seat == null) {
          continue;
        }
        // 检查订单ID是否匹配
        if (seat.getMovieOrderId() == null || !movieOrderId.equals(seat.getMovieOrderId())) {
          continue;
        }
        // 检查状态是否为 locked
        if (seat.getSelectSeatState() != null &&
            seat.getSelectSeatState().intValue() == SeatState.locked.getCode()) {
          result.add(seat);
        }
      } catch (Exception ignore) {
        // JSON 解析异常忽略
      }
    }

    return result;
  }

  /**
   * 从 Redis 获取现有选座记录
   * 只从 Redis 读取，不查询数据库（因为选座记录只存 Redis）
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatIdToNameMap 座位ID到座位名称的映射 (seatId -> seatName)
   * @return 现有选座记录列表，如果 Redis 中没有则返回空列表
   */
  public List<SelectSeat> getExistingSeats(Integer movieShowTimeId, Integer theaterHallId,
      Map<Integer, String> seatIdToNameMap) {
    // 只从 Redis 读取（选座记录只存 Redis，不存数据库）
    return getSeatsFromRedis(movieShowTimeId, theaterHallId, seatIdToNameMap);
  }

  /**
   * 检查 Redis 中是否有其他用户的 selected/locked 状态座位（冲突检查）
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatIds         要检查的座位ID列表
   * @param currentUserId   当前用户ID（排除自己）
   * @return true 如果有冲突（其他用户已选/已锁），false 无冲突
   */
  public boolean checkRedisSeatConflict(Integer movieShowTimeId, Integer theaterHallId,
      List<Integer> seatIds, Integer currentUserId) {
    if (movieShowTimeId == null || theaterHallId == null || seatIds == null || seatIds.isEmpty()) {
      return false;
    }

    // 按场次 + 影厅前缀扫描所有选座数据
    String pattern = RedisType.seatSelectionData.getCode() + ":" +
        movieShowTimeId + ":" + theaterHallId + ":*";

    Set<String> keys = redisTemplate.keys(pattern);
    if (keys == null || keys.isEmpty()) {
      return false;
    }

    for (String key : keys) {
      String seatJson = redisTemplate.opsForValue().get(key);
      if (seatJson == null) {
        continue;
      }
      try {
        SelectSeat seat = JSON.parseObject(seatJson, SelectSeat.class);
        if (seat == null || seat.getSeatId() == null) {
          continue;
        }
        // 检查是否是目标座位之一
        if (!seatIds.contains(seat.getSeatId())) {
          continue;
        }
        // 排除当前用户
        if (currentUserId != null && currentUserId.equals(seat.getUserId())) {
          continue;
        }
        // 检查状态：selected 或 locked 都算冲突
        if (seat.getSelectSeatState() != null &&
            (seat.getSelectSeatState().intValue() == SeatState.selected.getCode() ||
                seat.getSelectSeatState().intValue() == SeatState.locked.getCode())) {
          return true; // 发现冲突
        }
      } catch (Exception ignore) {
        // JSON 解析异常忽略
      }
    }

    return false; // 无冲突
  }

  /**
   * 检查座位冲突
   * 
   * @param existingSeats 现有选座记录
   * @param seatPositions 要选择的座位列表（包含 x, y, seatId, seatName）
   * @param userId        当前用户ID
   * @param theaterHallId 影厅ID
   * @throws RuntimeException 如果座位已被其他用户选择、被锁定或已支付
   */
  public void checkSeatConflict(List<SelectSeat> existingSeats, List<? extends SeatPositionInterface> seatPositions,
      Integer userId, Integer theaterHallId) {
    if (existingSeats.isEmpty()) {
      return; // 如果没有现有记录，视为新座位，可以正常选择
    }

    for (SeatPositionInterface item : seatPositions) {
      for (SelectSeat existingSeat : existingSeats) {
        if (theaterHallId.equals(existingSeat.getTheaterHallId()) &&
            item.getX().equals(existingSeat.getX()) &&
            item.getY().equals(existingSeat.getY()) &&
            existingSeat.getDeleted() != null && existingSeat.getDeleted() == 0) {
          
          // 检查座位状态：如果是 locked 或 sold，即使是同一用户也要提示
          if (existingSeat.getSelectSeatState() != null) {
            int state = existingSeat.getSelectSeatState().intValue();
            
            // 座位已被锁定（已创建订单，未支付）
            if (state == SeatState.locked.getCode()) {
              throw new BusinessException(ResponseCode.SEAT_OCCUPIED, MessageKeys.Error.SEAT_ALREADY_LOCKED, item.getSeatName());
            }
            
            // 座位已支付（已售出）
            if (state == SeatState.sold.getCode()) {
              throw new BusinessException(ResponseCode.SEAT_OCCUPIED, MessageKeys.Error.SEAT_ALREADY_SELECTED, item.getSeatName());
            }
          }
          
          // 如果存在，判断是否是同一个人选的
          if (!userId.equals(existingSeat.getUserId())) {
            // 不是同一个人，抛出异常
            throw new BusinessException(ResponseCode.SEAT_CONFLICT, MessageKeys.Error.SEAT_CONFLICT, item.getSeatName());
          }
        }
      }
    }
  }

  /**
   * 锁定座位（带回滚机制）
   * 
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatPositions   座位列表（包含 x, y, seatId, seatName）
   * @param userId          用户ID
   * @return 已成功锁定的座位列表
   * @throws RuntimeException 如果锁定失败
   */
  public <T extends SeatPositionInterface> List<T> lockSeatsWithRollback(Integer movieShowTimeId, Integer theaterHallId,
      List<T> seatPositions, Integer userId) {
    List<T> lockedSeats = new ArrayList<>();
    try {
      for (T item : seatPositions) {
        boolean locked = lockSingleSeat(movieShowTimeId, theaterHallId, item.getX(), item.getY(), userId);

        if (!locked) {
          // 座位已被其他用户锁定，释放已锁定的座位并抛出异常
          for (T lockedSeat : lockedSeats) {
            unlockSingleSeat(movieShowTimeId, theaterHallId, lockedSeat.getX(), lockedSeat.getY());
          }
          throw new BusinessException(ResponseCode.SEAT_OCCUPIED, MessageKeys.Error.SEAT_ALREADY_SELECTED, item.getSeatName());
        }
        lockedSeats.add(item);
      }
    } catch (Exception e) {
      // 如果锁定过程中出现异常，释放已锁定的座位
      for (T lockedSeat : lockedSeats) {
        unlockSingleSeat(movieShowTimeId, theaterHallId, lockedSeat.getX(), lockedSeat.getY());
      }
      throw e;
    }
    return lockedSeats;
  }

  /**
   * 座位位置接口（用于方法参数）
   */
  public interface SeatPositionInterface {
    Integer getX();

    Integer getY();

    Integer getSeatId();

    String getSeatName();
  }

  /**
   * 保存选座（主方法）
   * 使用 Redis 分布式锁防止重复调用
   * 
   * @param userId          用户ID
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatPositions   座位列表（包含 x, y, seatId, seatName）
   * @throws RuntimeException 如果保存失败
   */
  @Transactional(rollbackFor = Exception.class)
  public <T extends SeatPositionInterface> void saveSeatSelection(Integer userId, Integer movieShowTimeId,
      Integer theaterHallId,
      List<T> seatPositions) {
    // 0. 幂等性控制：使用 Redis 分布式锁防止重复调用
    // 构建锁的 key：基于 userId + movieShowTimeId + 排序后的座位ID列表
    List<Integer> sortedSeatIds = seatPositions.stream()
        .map(T::getSeatId)
        .filter(id -> id != null)
        .sorted()
        .collect(Collectors.toList());
    
    String lockKey = RedisType.seatSelectionSaveLock.getCode() + ":" + userId + ":" +
        movieShowTimeId + ":" + theaterHallId + ":" +
        sortedSeatIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    
    // 尝试获取锁（10秒超时，防止死锁）
    Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(userId), 10, TimeUnit.SECONDS);
    if (!Boolean.TRUE.equals(lockAcquired)) {
      log.warn("选座保存请求重复调用，已忽略: userId={}, movieShowTimeId={}, theaterHallId={}, seatIds={}",
          userId, movieShowTimeId, theaterHallId, sortedSeatIds);
      // 检查是否已经保存成功（幂等性：如果已保存，直接返回成功）
      Map<Integer, String> seatIdToNameMap = seatPositions.stream()
          .collect(Collectors.toMap(
              T::getSeatId,
              T::getSeatName,
              (existing, replacement) -> existing));
      List<SelectSeat> existingSeats = getExistingSeats(movieShowTimeId, theaterHallId, seatIdToNameMap);
      
      // 如果所有座位都已经保存且状态为 selected，说明是重复调用，直接返回
      if (!existingSeats.isEmpty() && existingSeats.size() == sortedSeatIds.size()) {
        boolean allSelected = existingSeats.stream()
            .allMatch(seat -> seat.getUserId() != null && seat.getUserId().equals(userId) &&
                seat.getSelectSeatState() != null &&
                seat.getSelectSeatState().intValue() == SeatState.selected.getCode());
        if (allSelected) {
          log.info("选座已保存，重复调用已忽略（幂等性）: userId={}, movieShowTimeId={}, theaterHallId={}",
              userId, movieShowTimeId, theaterHallId);
          return; // 幂等性：已保存，直接返回
        }
      }
      
      // 如果锁被占用且座位未保存，说明有其他请求正在处理，抛出异常
      throw new BusinessException(ResponseCode.SEAT_PROCESSING, MessageKeys.Error.SEAT_SELECTION_PROCESSING);
    }

    try {
      // 1. 构建 seatId -> seatName 的映射
      Map<Integer, String> seatIdToNameMap = seatPositions.stream()
          .collect(Collectors.toMap(
              T::getSeatId,
              T::getSeatName,
              (existing, replacement) -> existing));

      // 2. 获取现有选座记录
      List<SelectSeat> existingSeats = getExistingSeats(movieShowTimeId, theaterHallId, seatIdToNameMap);

      // 3. 如果现有选座记录为空，直接保存到 Redis，状态设置为已选择
      if (existingSeats.isEmpty()) {
      // 构建选座信息（只保存到 Redis，不保存到数据库，）
      List<SelectSeat> data = seatPositions.stream().map((T item) -> {
        SelectSeat modal = new SelectSeat();
        modal.setMovieShowTimeId(movieShowTimeId);
        modal.setTheaterHallId(theaterHallId);
        modal.setX(item.getX());
        modal.setY(item.getY());
        modal.setUserId(userId);
        modal.setSelectSeatState(SeatState.selected.getCode());
        modal.setSeatId(item.getSeatId());
        modal.setSeatName(item.getSeatName());
        return modal;
      }).toList();

      // 将选座信息写入 Redis（不保存到数据库）
      for (SelectSeat seat : data) {
        saveSeatToRedis(seat);
      }

      // 转换为座位坐标列表
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates = seatPositions.stream().map((T item) -> {
        SeatSelectionCancelMessage.SeatCoordinate coord = new SeatSelectionCancelMessage.SeatCoordinate();
        coord.setX(item.getX());
        coord.setY(item.getY());
        coord.setSeatId(item.getSeatId());
        coord.setSeatName(item.getSeatName());
        return coord;
      }).toList();

      // 在 Redis 中存储整体锁定信息
      lockSeats(userId, movieShowTimeId, theaterHallId, seatCoordinates);

      // 发送延迟取消消息到 RabbitMQ
      sendCancelMessage(userId, movieShowTimeId, theaterHallId, seatCoordinates);
      
      return; // 直接返回，不需要执行后续的冲突检查和锁定逻辑
    }

      // 4. 检查座位冲突（如果现有记录不为空）
      checkSeatConflict(existingSeats, seatPositions, userId, theaterHallId);

      // 5. 从 Redis 中删除之前的选座数据缓存（不删除数据库记录，因为只存 Redis）
      Map<Integer, String> previousSeatIdToNameMap = seatPositions.stream()
          .filter(item -> item.getSeatId() != null && item.getSeatName() != null)
          .collect(Collectors.toMap(
              T::getSeatId,
              T::getSeatName,
              (existing, replacement) -> existing));
      if (!previousSeatIdToNameMap.isEmpty()) {
        deleteSeatsFromRedis(movieShowTimeId, theaterHallId, previousSeatIdToNameMap);
      }

      // 6. 构建新的选座信息（只保存到 Redis，不保存到数据库）
      List<SelectSeat> data = seatPositions.stream().map((T item) -> {
        SelectSeat modal = new SelectSeat();
        modal.setMovieShowTimeId(movieShowTimeId);
        modal.setTheaterHallId(theaterHallId);
        modal.setX(item.getX());
        modal.setY(item.getY());
        modal.setUserId(userId);
        modal.setSelectSeatState(SeatState.selected.getCode());
        modal.setSeatId(item.getSeatId());
        modal.setSeatName(item.getSeatName());
        return modal;
      }).toList();

      // 7. 将选座信息写入 Redis（不保存到数据库）
      for (SelectSeat seat : data) {
        saveSeatToRedis(seat);
      }

      // 8. 转换为座位坐标列表
      List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates = seatPositions.stream().map((T item) -> {
        SeatSelectionCancelMessage.SeatCoordinate coord = new SeatSelectionCancelMessage.SeatCoordinate();
        coord.setX(item.getX());
        coord.setY(item.getY());
        coord.setSeatId(item.getSeatId());
        coord.setSeatName(item.getSeatName());
        return coord;
      }).toList();

      // 9. 在 Redis 中存储整体锁定信息（用于延迟取消）
      lockSeats(userId, movieShowTimeId, theaterHallId, seatCoordinates);

      // 10. 发送延迟取消消息到 RabbitMQ
      sendCancelMessage(userId, movieShowTimeId, theaterHallId, seatCoordinates);
    } catch (Exception e) {
      // 如果保存失败，记录错误日志
      log.error("选座保存失败: userId={}, movieShowTimeId={}, error={}",
          userId, movieShowTimeId, e.getMessage(), e);
      throw e;
    } finally {
      // 确保释放分布式锁
      redisTemplate.delete(lockKey);
      log.debug("选座保存锁已释放: lockKey={}", lockKey);
    }
  }

  /**
   * 取消选座（主方法）
   * 
   * @param userId          用户ID
   * @param movieShowTimeId 场次ID
   * @param theaterHallId   影厅ID
   * @param seatPositions   座位列表（包含 x, y, seatId, seatName）
   */
  @Transactional(rollbackFor = Exception.class)
  public <T extends SeatPositionInterface> void cancelSeatSelection(Integer userId, Integer movieShowTimeId,
      Integer theaterHallId,
      List<T> seatPositions) {
    // 1. 构建座位坐标列表（用于删除 Redis 锁定）
    List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates = seatPositions.stream().map((T item) -> {
      SeatSelectionCancelMessage.SeatCoordinate coord = new SeatSelectionCancelMessage.SeatCoordinate();
      coord.setX(item.getX());
      coord.setY(item.getY());
      coord.setSeatId(item.getSeatId());
      coord.setSeatName(item.getSeatName());
      return coord;
    }).toList();

    // 2. 从 Redis 中删除选座数据缓存（不删除数据库记录，因为只存 Redis）
    Map<Integer, String> cancelSeatIdToNameMap = seatPositions.stream()
        .filter(item -> item.getSeatId() != null && item.getSeatName() != null)
        .collect(Collectors.toMap(
            T::getSeatId,
            T::getSeatName,
            (existing, replacement) -> existing));
    if (!cancelSeatIdToNameMap.isEmpty()) {
      deleteSeatsFromRedis(movieShowTimeId, theaterHallId, cancelSeatIdToNameMap);
    }

    // 4. 从 Redis 中删除锁定信息（单个座位锁定 + 整体锁定）
    unlockSeats(userId, movieShowTimeId, theaterHallId, seatCoordinates);
  }
}
