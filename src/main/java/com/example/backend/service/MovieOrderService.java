package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.SeatSelectionCancelMessage;
import com.example.backend.dto.OrderCreateMessage;
import com.example.backend.config.RabbitMQConfig;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.entity.Refund;
import com.example.backend.enumerate.RefundApplyStatus;
import com.example.backend.enumerate.RefundState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.query.SeatGroup;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.query.order.MyTicketsQuery;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.order.MovieOrderSeat;
import com.example.backend.response.order.MyTicketsResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.example.backend.enumerate.RedisType;
import com.example.backend.exception.BusinessException;
import com.example.backend.constants.MessageKeys;

@Data
class SeatGroupQuery {
  Integer x;
  Integer y;
  Integer seatId;
  Integer movieTicketTypeId;
  Integer theaterHallId;
  BigDecimal movieTicketTypePrice;
  BigDecimal areaPrice;
  BigDecimal plusPrice;
}

@Slf4j
@Service
public class MovieOrderService extends ServiceImpl<MovieOrderMapper, MovieOrder> {
  @Autowired
  MovieShowTimeMapper movieShowTimeMapper;
  @Autowired
  SelectSeatService selectSeatService;

  @Autowired
  SelectSeatMapper selectSeatMapper;

  @Autowired
  MovieTicketTypeMapper movieTicketTypeMapper;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @Autowired
  com.example.backend.mapper.RefundMapper refundMapper;

  @Autowired
  SeatMapper seatMapper;

  @Autowired
  PaymentMethodMapper paymentMethodMapper;

  @Autowired
  PaymentService paymentMethodService;

  @Autowired
  CinemaMapper cinemaMapper;

  @Autowired
  TicketPriceService ticketPriceService;

  @Autowired
  private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

  @Autowired
  private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

  @org.springframework.beans.factory.annotation.Value("${order.payment-timeout:900}")
  private int paymentTimeoutSeconds;

  @org.springframework.beans.factory.annotation.Value("${order.payment-rate-limit:10}")
  private int paymentRateLimit;

  @org.springframework.beans.factory.annotation.Value("${order.payment-lock-timeout:60}")
  private int paymentLockTimeoutSeconds;

  /** 支付异步处理的共享线程池 */
  private static final ScheduledExecutorService PAYMENT_SCHEDULER =
      Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "order-payment-async");
        t.setDaemon(false);
        return t;
      });

  /**
   * 获取订单所属用户ID（支持旧数据：从 select_seat 或 Redis 推断）
   */
  private Integer getOrderOwnerId(MovieOrder order) {
    if (order.getUserId() != null) {
      return order.getUserId();
    }
    // 兼容旧数据：从 select_seat 获取
    QueryWrapper<SelectSeat> qw = new QueryWrapper<>();
    qw.eq("movie_order_id", order.getId()).last("LIMIT 1");
    SelectSeat seat = selectSeatMapper.selectOne(qw);
    if (seat != null && seat.getUserId() != null) {
      return seat.getUserId();
    }
    // 兼容 order_created：从 Redis locked 座位获取
    if (order.getMovieShowTimeId() != null) {
      MovieShowTime st = movieShowTimeMapper.selectById(order.getMovieShowTimeId());
      Integer theaterHallId = st != null ? st.getTheaterHallId() : null;
      if (theaterHallId != null) {
        List<SelectSeat> locked = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            order.getMovieShowTimeId(), theaterHallId, order.getId());
        if (!locked.isEmpty() && locked.get(0).getUserId() != null) {
          return locked.get(0).getUserId();
        }
      }
    }
    return null;
  }

  /**
   * 校验订单归属，非本人或无法确定归属则抛出异常
   */
  private void verifyOrderOwnership(MovieOrder order, Integer currentUserId) {
    Integer ownerId = getOrderOwnerId(order);
    if (ownerId == null) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
    if (!ownerId.equals(currentUserId)) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
  }

  /**
   * 校验订单状态：仅允许从 order_created 转换到其他状态
   */
  private void verifyOrderStateFromCreated(MovieOrder order) {
    if (!Objects.equals(order.getOrderState(), OrderState.order_created.getCode())) {
      throw new BusinessException(ResponseCode.ORDER_STATE_INVALID, MessageKeys.Error.ORDER_CANNOT_CANCEL);
    }
  }

  /**
   * 清理订单关联的 Redis locked 座位
   */
  private void clearLockedSeatsFromRedis(Integer orderId, Integer movieShowTimeId) {
    if (movieShowTimeId == null) {
      return;
    }
    MovieShowTime st = movieShowTimeMapper.selectById(movieShowTimeId);
    Integer theaterHallId = st != null ? st.getTheaterHallId() : null;
    if (theaterHallId == null) {
      return;
    }
    List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
        movieShowTimeId, theaterHallId, orderId);
    if (!lockedSeats.isEmpty()) {
      Map<Integer, String> seatMap = lockedSeats.stream()
          .filter(s -> s.getSeatId() != null && s.getSeatName() != null)
          .collect(Collectors.toMap(SelectSeat::getSeatId, SelectSeat::getSeatName, (a, b) -> a));
      if (!seatMap.isEmpty()) {
        SelectSeat first = lockedSeats.get(0);
        selectSeatService.deleteSeatsFromRedis(first.getMovieShowTimeId(), first.getTheaterHallId(), seatMap);
        log.info("已清理 Redis locked 座位: orderId={}", orderId);
      }
    }
  }

  /**
   * 支付失败时调用退款（释放/回滚已授权的资金），写入 refund 表
   */
  private void refundOnPaymentFailure(Integer orderId, MovieOrder order, String reason) {
    String orderNumber = order.getOrderNumber();
    if (orderNumber == null) {
      log.warn("订单无订单号，跳过退款: orderId={}", orderId);
      return;
    }
    try {
      BigDecimal amount = order.getOrderTotal() != null ? order.getOrderTotal() : order.getPayTotal();
      if (amount == null) {
        amount = BigDecimal.ZERO;
      }

      Refund refund = new Refund();
      refund.setOrderNumber(orderNumber);
      refund.setUserId(order.getUserId());
      refund.setAmount(amount);
      refund.setReason(reason);
      refund.setApplyStatus(RefundApplyStatus.approved.getCode());
      refund.setRefundState(RefundState.refunding.getCode());
      refund.setApplyTime(new Date());
      refundMapper.insert(refund);

      boolean success = paymentMethodService.refund(orderId, amount, reason);

      refund.setRefundState(success ? RefundState.refunded.getCode() : RefundState.refund_failed.getCode());
      refund.setProcessTime(new Date());
      refundMapper.updateById(refund);
    } catch (Exception e) {
      log.warn("退款调用异常: orderNumber={}, reason={}", orderNumber, reason, e);
      try {
        Refund latest = refundMapper.selectOne(
            new QueryWrapper<Refund>().eq("order_number", orderNumber).orderByDesc("id").last("LIMIT 1"));
        if (latest != null) {
          latest.setRefundState(RefundState.refund_failed.getCode());
          latest.setProcessTime(new Date());
          refundMapper.updateById(latest);
        } else {
          Refund refund = new Refund();
          refund.setOrderNumber(orderNumber);
          refund.setUserId(order.getUserId());
          refund.setAmount(order.getOrderTotal() != null ? order.getOrderTotal() : BigDecimal.ZERO);
          refund.setReason(reason);
          refund.setApplyStatus(RefundApplyStatus.approved.getCode());
          refund.setRefundState(RefundState.refund_failed.getCode());
          refund.setApplyTime(new Date());
          refund.setProcessTime(new Date());
          refundMapper.insert(refund);
        }
      } catch (Exception ex) {
        log.error("更新退款失败状态异常: orderNumber={}", orderNumber, ex);
      }
    }
  }

  /**
   * 校验当前用户是否有权访问订单（用于订单详情等）
   */
  public void verifyOrderAccess(Integer orderId, Integer currentUserId) {
    MovieOrder order = movieOrderMapper.selectById(orderId);
    if (order == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    verifyOrderOwnership(order, currentUserId);
  }

  /**
   * 生成全局唯一且相对友好的业务订单号
   * <p>
   * 设计目标：
   * <ul>
   *   <li>不能从订单号中直接看出日期、当天订单量（避免被“扫号”和敏感信息泄露）；</li>
   *   <li>长度适中，方便用户在 App / 电话中报号、核对；</li>
   *   <li>支持区分不同类型的订单（电影票、会员卡等）。</li>
   * </ul>
   * 规则示例：{@code MT26A8K3X7P9Q}
   * <ul>
   *   <li>前缀：{@code MT} 表示“电影票订单”（Movie Ticket），后续可以扩展 {@code VC}（会员卡）、{@code GF}（礼品卡）等；</li>
   *   <li>主体：12 位 0-9A-Z 的编码，由“时间戳 + 随机数”混合后转 36 进制得到，难以从中推断真实时间和订单总量。</li>
   * </ul>
   *
   * @param typePrefix 订单类型前缀，例如：{@code MT}(电影票)、{@code VC}(会员卡)，为空时默认 {@code OD}
   * @return 业务订单号（用于对外展示和业务关联）
   */
  private String generateOrderNumber(String typePrefix) {
    String prefix = (typePrefix == null || typePrefix.isEmpty()) ? "OD" : typePrefix.toUpperCase();
    long now = System.currentTimeMillis();
    int random = new Random().nextInt(1_000_000); // 0 ~ 999999
    // 将时间戳和随机数组合后做一次混淆
    long mixed = (now << 20) ^ random;
    String base36 = Long.toString(mixed, 36).toUpperCase();
    // 保证长度至少 12 位，不够则左侧补 0
    String padded = String.format("%12s", base36).replace(' ', '0');
    return prefix + padded;
  }

  /**
   * 创建订单（异步方式）
   * 1. 先检查 Redis 冲突（是否有其他用户的 selected/locked 状态）
   * 2. 将座位状态改为 locked，只存 Redis（不存数据库）
   * 3. 通过 RabbitMQ 异步创建订单
   * 
   * @param query 订单创建请求
   * @return 订单对象（包含订单号，订单可能还在创建中）
   * @throws Exception 如果冲突或参数错误
   */
  public MovieOrder createOrder(MovieOrderSaveQuery query) throws Exception {
    Integer movieShowTimeId = query.getMovieShowTimeId();
    Integer userId = StpUtil.getLoginIdAsInt();

    // 1. 基础信息：场次 / 影院 / 影厅（不再依赖用户选座 SQL）
    com.example.backend.response.UserSelectSeat baseInfo = movieShowTimeMapper.userSelectSeatWithoutSpec(
        userId,
        movieShowTimeId,
        SeatState.selected.getCode());
    if (baseInfo == null) {
      throw new BusinessException(ResponseCode.SHOWTIME_NOT_FOUND, MessageKeys.Error.SHOWTIME_NOT_FOUND);
    }
    Integer theaterHallId = baseInfo.getTheaterHallId();
    Integer cinemaId = baseInfo.getCinemaId();

    // 1.1 对前端传入的座位按 seatId 去重，防止重复提交导致订单座位重复
    List<SeatGroup> uniqueSeatGroups = query.getSeat().stream()
        .filter(s -> s.getSeatId() != null)
        .collect(Collectors.toMap(SeatGroup::getSeatId, s -> s, (a, b) -> a, LinkedHashMap::new))
        .values().stream().collect(Collectors.toList());
    if (uniqueSeatGroups.isEmpty()) {
      throw new BusinessException(ResponseCode.SEAT_NOT_SELECTED, MessageKeys.Error.NO_VALID_SEAT_SELECTION);
    }

    // 2. 从 Redis 校验当前用户的已选座位（selected 状态）
    List<SelectSeat> redisSelectedSeats = selectSeatService.getUserSelectedSeatsFromRedis(
        movieShowTimeId,
        theaterHallId,
        userId);
    if (redisSelectedSeats == null || redisSelectedSeats.isEmpty()) {
      throw new BusinessException(ResponseCode.SEAT_NOT_SELECTED, MessageKeys.Error.NO_VALID_SEAT_SELECTION);
    }
    Map<Integer, SelectSeat> redisSeatMap = redisSelectedSeats.stream()
        .filter(s -> s.getSeatId() != null)
        .collect(Collectors.toMap(SelectSeat::getSeatId, s -> s, (a, b) -> a));

    // 3. 查询座位区域信息，用于计算区域价
    List<SeatListResponse> seatList = seatMapper.seatList(theaterHallId);
    Map<Integer, SeatListResponse> seatInfoMap = seatList.stream()
        .collect(Collectors.toMap(SeatListResponse::getId, s -> s, (a, b) -> a));

    // 4. 票价由 TicketPriceService 计算：票种 + 3D加价 + 规格加价
    Integer dimensionType = baseInfo.getDimensionType();
    List<Integer> specIds = baseInfo.getSpecIds();

    // 5. 幂等性检查：检查是否已为这些座位创建过订单
    List<Integer> requestSeatIds = uniqueSeatGroups.stream()
        .map(SeatGroup::getSeatId)
        .filter(id -> id != null)
        .collect(Collectors.toList());
    
    // 检查 Redis 中这些座位是否已经是 locked 状态（说明已创建订单）
    List<SelectSeat> lockedSeats = selectSeatService.getUserLockedSeatsFromRedis(
        movieShowTimeId, theaterHallId, userId, requestSeatIds);
    
    // 如果所有请求的座位都已经是 locked 状态，说明已创建订单，查询并返回已有订单
    if (lockedSeats != null && !lockedSeats.isEmpty() && 
        lockedSeats.size() == requestSeatIds.size()) {
      // 从 locked 座位中获取 movieOrderId（如果有）
      Integer existingOrderId = null;
      for (SelectSeat seat : lockedSeats) {
        if (seat.getMovieOrderId() != null) {
          existingOrderId = seat.getMovieOrderId();
          break;
        }
      }
      
      // 如果有 orderId，直接查询数据库获取订单
      if (existingOrderId != null) {
        MovieOrder existingOrder = movieOrderMapper.selectById(existingOrderId);
        if (existingOrder != null) {
          // 计算支付截止时间
          if (existingOrder.getCreateTime() != null) {
            Date payDeadline = new Date(existingOrder.getCreateTime().getTime() + paymentTimeoutSeconds * 1000L);
            existingOrder.setPayDeadline(payDeadline);
          }
          return existingOrder;
        }
      }
      
      // 如果没有 orderId，可能是订单还在创建中，通过座位查询订单
      QueryWrapper<SelectSeat> seatWrapper = new QueryWrapper<>();
      seatWrapper.eq("movie_show_time_id", movieShowTimeId)
          .eq("theater_hall_id", theaterHallId)
          .eq("user_id", userId)
          .in("seat_id", requestSeatIds)
          .eq("select_seat_state", SeatState.locked.getCode())
          .isNotNull("movie_order_id")
          .last("LIMIT 1");
      SelectSeat seatWithOrder = selectSeatMapper.selectOne(seatWrapper);
      
      if (seatWithOrder != null && seatWithOrder.getMovieOrderId() != null) {
        MovieOrder existingOrder = movieOrderMapper.selectById(seatWithOrder.getMovieOrderId());
        if (existingOrder != null) {
          // 计算支付截止时间
          if (existingOrder.getCreateTime() != null) {
            Date payDeadline = new Date(existingOrder.getCreateTime().getTime() + paymentTimeoutSeconds * 1000L);
            existingOrder.setPayDeadline(payDeadline);
          }
          return existingOrder;
        }
      }
    }
    
    // 6. 使用 Redis 分布式锁防止并发重复创建
    // 构建锁的 key：基于 userId + movieShowTimeId + 排序后的座位ID列表
    List<Integer> sortedSeatIds = new ArrayList<>(requestSeatIds);
    Collections.sort(sortedSeatIds);
    String lockKey = RedisType.orderCreateLock.getCode() + ":" + userId + ":" + 
        movieShowTimeId + ":" + sortedSeatIds.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
    
    // 尝试获取锁（30秒超时，防止死锁）
    Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(userId), 30, TimeUnit.SECONDS);
    if (!Boolean.TRUE.equals(lockAcquired)) {
      // 获取锁失败，说明有其他请求正在创建订单，等待一下后再次检查
      try {
        Thread.sleep(100); // 等待 100ms
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      
      // 再次检查是否已创建订单
      lockedSeats = selectSeatService.getUserLockedSeatsFromRedis(
          movieShowTimeId, theaterHallId, userId, requestSeatIds);
      if (lockedSeats != null && !lockedSeats.isEmpty() && 
          lockedSeats.size() == requestSeatIds.size()) {
        // 查询已有订单
        QueryWrapper<SelectSeat> seatWrapper = new QueryWrapper<>();
        seatWrapper.eq("movie_show_time_id", movieShowTimeId)
            .eq("theater_hall_id", theaterHallId)
            .eq("user_id", userId)
            .in("seat_id", requestSeatIds)
            .eq("select_seat_state", SeatState.locked.getCode())
            .isNotNull("movie_order_id")
            .last("LIMIT 1");
        SelectSeat seatWithOrder = selectSeatMapper.selectOne(seatWrapper);
        
        if (seatWithOrder != null && seatWithOrder.getMovieOrderId() != null) {
          MovieOrder existingOrder = movieOrderMapper.selectById(seatWithOrder.getMovieOrderId());
          if (existingOrder != null) {
            // 计算支付截止时间
            if (existingOrder.getCreateTime() != null) {
              Date payDeadline = new Date(existingOrder.getCreateTime().getTime() + paymentTimeoutSeconds * 1000L);
              existingOrder.setPayDeadline(payDeadline);
            }
            return existingOrder;
          }
        }
      }
      
      throw new BusinessException(ResponseCode.ORDER_CREATING, MessageKeys.Error.ORDER_CREATING);
    }
    
    try {
      // 7. 组装每个座位的价格明细
      List<SeatGroupQuery> data = uniqueSeatGroups.stream().map(item -> {
      SelectSeat selected = redisSeatMap.get(item.getSeatId());
      if (selected == null) {
        throw new BusinessException(ResponseCode.SEAT_INVALID_OR_NOT_SELECTED, MessageKeys.Error.SEAT_INVALID);
      }
      // 校验坐标一致性（防止前端伪造）
      if (!item.getX().equals(selected.getX()) || !item.getY().equals(selected.getY())) {
        throw new BusinessException(ResponseCode.SEAT_COORDINATE_MISMATCH, MessageKeys.Error.SEAT_COORDINATE_MISMATCH);
      }

      MovieTicketType movieTicketType = movieTicketTypeMapper.selectById(item.getMovieTicketTypeId());
      if (movieTicketType == null) {
        throw new BusinessException(ResponseCode.TICKET_TYPE_NOT_FOUND, MessageKeys.Error.TICKET_TYPE_NOT_FOUND);
      }

      SeatListResponse seatInfo = seatInfoMap.get(item.getSeatId());
      BigDecimal areaPrice = BigDecimal.ZERO;
      if (seatInfo != null && seatInfo.getAreaPrice() != null) {
        areaPrice = BigDecimal.valueOf(seatInfo.getAreaPrice());
      }

      BigDecimal ticketPrice = ticketPriceService.calculatePrice(
          cinemaId, movieTicketType.getId(), dimensionType, specIds);

      SeatGroupQuery modal = new SeatGroupQuery();
      modal.setX(item.getX());
      modal.setY(item.getY());
      modal.setSeatId(item.getSeatId());
      modal.setTheaterHallId(theaterHallId);
      modal.setMovieTicketTypeId(movieTicketType.getId());
      modal.setMovieTicketTypePrice(ticketPrice);
      modal.setAreaPrice(areaPrice);
      modal.setPlusPrice(BigDecimal.ZERO);
      return modal;
    }).toList();

    // 6. 计算总价
    BigDecimal total = data.stream()
        .map(current -> {
          BigDecimal sum = BigDecimal.ZERO;
          if (current.getAreaPrice() != null) {
            sum = sum.add(current.getAreaPrice());
          }
          if (current.getMovieTicketTypePrice() != null) {
            sum = sum.add(current.getMovieTicketTypePrice());
          }
          if (current.getPlusPrice() != null) {
            sum = sum.add(current.getPlusPrice());
          }
          return sum;
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 7. 检查 Redis 冲突：是否有其他用户的 selected/locked 状态
    // 检查每个座位的可用性，找出不可用的座位
    List<Integer> seatIds = data.stream().map(SeatGroupQuery::getSeatId).collect(Collectors.toList());
    List<Integer> unavailableSeatIds = new ArrayList<>();
    List<String> unavailableSeatNames = new ArrayList<>();
    
    for (SeatGroupQuery seatItem : data) {
      SelectSeat selected = redisSeatMap.get(seatItem.getSeatId());
      if (selected == null) {
        // 座位不在 Redis 中，可能已被删除或未选中
        unavailableSeatIds.add(seatItem.getSeatId());
        unavailableSeatNames.add(seatItem.getSeatId() != null ? "座位ID:" + seatItem.getSeatId() : "未知座位");
        continue;
      }
      
      // 检查座位状态：如果是 locked 或 sold，且不是当前用户的，则不可用
      Integer seatState = selected.getSelectSeatState();
      if (seatState != null) {
        if (seatState.equals(SeatState.locked.getCode()) || seatState.equals(SeatState.sold.getCode())) {
          // 检查是否是当前用户的座位
          if (!userId.equals(selected.getUserId())) {
            unavailableSeatIds.add(seatItem.getSeatId());
            unavailableSeatNames.add(selected.getSeatName() != null ? selected.getSeatName() : "座位ID:" + seatItem.getSeatId());
          }
        }
      }
    }
    
    // 如果有不可用的座位，抛出异常，告知哪些座位不可用
    if (!unavailableSeatIds.isEmpty()) {
      String unavailableSeatsStr = String.join(", ", unavailableSeatNames);
      // 构建额外数据，包含不可用座位的详细信息
      Map<String, Object> extraData = new HashMap<>();
      extraData.put("unavailableSeatIds", unavailableSeatIds);
      extraData.put("unavailableSeatNames", unavailableSeatNames);
      extraData.put("unavailableCount", unavailableSeatIds.size());
      extraData.put("totalCount", data.size());
      throw new BusinessException(ResponseCode.SEAT_OCCUPIED, MessageKeys.Error.SEAT_OCCUPIED, extraData, unavailableSeatsStr);
    }

    // 8. 生成订单号
    String orderNumber = generateOrderNumber("MT");

    // 9. 将座位状态改为 locked，只存 Redis（不存数据库）
    List<SelectSeat> lockedSeatsForOrder = data.stream().map(item -> {
      SelectSeat seat = redisSeatMap.get(item.getSeatId());
      if (seat == null) {
        throw new BusinessException(ResponseCode.SEAT_INVALID_OR_NOT_SELECTED, MessageKeys.Error.SEAT_INVALID);
      }
      // 更新状态为 locked
      seat.setSelectSeatState(SeatState.locked.getCode());
      // 设置订单号（临时，等订单创建后会有真实的 orderId）
      // 注意：这里先不设置 movieOrderId，等消费者创建订单后再更新
      seat.setSeatName(seat.getSeatName()); // 确保 seatName 存在
      return seat;
    }).collect(Collectors.toList());

    // 保存到 Redis（覆盖 selected 状态）
    for (SelectSeat seat : lockedSeatsForOrder) {
      selectSeatService.saveSeatToRedis(seat);
    }

    // 10. 清理选座阶段的 Redis 锁定和延迟取消任务
    List<SeatSelectionCancelMessage.SeatCoordinate> seatCoordinates = data.stream().map(item -> {
      SeatSelectionCancelMessage.SeatCoordinate coord = new SeatSelectionCancelMessage.SeatCoordinate();
      coord.setX(item.getX());
      coord.setY(item.getY());
      coord.setSeatId(item.getSeatId());
      SelectSeat seat = redisSeatMap.get(item.getSeatId());
      if (seat != null && seat.getSeatName() != null) {
        coord.setSeatName(seat.getSeatName());
      }
      return coord;
    }).collect(Collectors.toList());
    selectSeatService.unlockSeats(userId, movieShowTimeId, theaterHallId, seatCoordinates);

    // 11. 发送订单创建消息到 RabbitMQ（异步创建订单）
    OrderCreateMessage orderMessage = new OrderCreateMessage();
    orderMessage.setUserId(userId);
    orderMessage.setMovieShowTimeId(movieShowTimeId);
    orderMessage.setTheaterHallId(theaterHallId);
    orderMessage.setCinemaId(cinemaId);
    orderMessage.setOrderNumber(orderNumber);
    orderMessage.setOrderTotal(total);
    orderMessage.setSeats(data.stream().map(item -> {
      OrderCreateMessage.SeatInfo seatInfo = new OrderCreateMessage.SeatInfo();
      seatInfo.setSeatId(item.getSeatId());
      SelectSeat seat = redisSeatMap.get(item.getSeatId());
      if (seat != null && seat.getSeatName() != null) {
        seatInfo.setSeatName(seat.getSeatName());
      }
      seatInfo.setX(item.getX());
      seatInfo.setY(item.getY());
      seatInfo.setMovieTicketTypeId(item.getMovieTicketTypeId());
      seatInfo.setAreaPrice(item.getAreaPrice());
      seatInfo.setMovieTicketTypePrice(item.getMovieTicketTypePrice());
      seatInfo.setPlusPrice(item.getPlusPrice());
      return seatInfo;
    }).collect(Collectors.toList()));

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.ORDER_CREATE_EXCHANGE,
        RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
        orderMessage
    );

    // 12. 返回订单对象（订单可能还在创建中，但订单号已生成）
    MovieOrder movieOrder = new MovieOrder();
    movieOrder.setOrderNumber(orderNumber);
    movieOrder.setOrderTotal(total);
    movieOrder.setMovieShowTimeId(movieShowTimeId);
    // 计算支付截止时间（订单创建时间 + 支付超时时间），用于前端倒计时显示
    Date now = new Date();
    Date payDeadline = new Date(now.getTime() + paymentTimeoutSeconds * 1000L);
    movieOrder.setPayDeadline(payDeadline);
    // 注意：orderId 和 orderState 等字段会在消费者创建订单后才有值
    // 这里只返回订单号，供前端展示和后续查询使用

    return movieOrder;
    } finally {
      // 释放分布式锁
      redisTemplate.delete(lockKey);
    }
  }

  
  @Transactional
  public void pay(String orderNumber, Integer payId) {
    Integer userId = StpUtil.getLoginIdAsInt();

    // 1. 支付限流：按用户维度，每分钟最多 N 次请求
    String rateLimitKey = RedisType.paymentRateLimit.getCode() + ":" + userId;
    Long count = redisTemplate.opsForValue().increment(rateLimitKey);
    if (count != null && count == 1) {
      redisTemplate.expire(rateLimitKey, 60, TimeUnit.SECONDS);
    }
    if (count != null && count > paymentRateLimit) {
      throw new BusinessException(ResponseCode.RATE_LIMIT_EXCEEDED, MessageKeys.Error.PAYMENT_RATE_LIMIT);
    }

    // 2. 通过订单号查询订单
    QueryWrapper<MovieOrder> orderWrapper = new QueryWrapper<>();
    orderWrapper.eq("order_number", orderNumber);
    MovieOrder order = movieOrderMapper.selectOne(orderWrapper);
    if (order == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }

    // 3. 订单归属校验
    verifyOrderOwnership(order, userId);

    Integer orderId = order.getId();

    // 4. 防止重复支付：已支付订单直接返回成功（幂等）
    if (Objects.equals(order.getOrderState(), OrderState.order_succeed.getCode())
        && Objects.equals(order.getPayState(), PayState.payment_successful.getCode())) {
      log.info("订单已支付，幂等返回: orderNumber={}, orderId={}", orderNumber, orderId);
      return;
    }

    // 5. 校验座位锁定：订单已超时或座位锁定已过期则拒绝支付
    if (Objects.equals(order.getOrderState(), OrderState.order_timeout.getCode())) {
      throw new BusinessException(ResponseCode.ORDER_TIMEOUT, MessageKeys.Error.ORDER_OR_SEAT_EXPIRED);
    }
    if (order.getCreateTime() != null
        && System.currentTimeMillis() > order.getCreateTime().getTime() + paymentTimeoutSeconds * 1000L) {
      throw new BusinessException(ResponseCode.ORDER_TIMEOUT, MessageKeys.Error.ORDER_OR_SEAT_EXPIRED);
    }
    Integer theaterHallId = null;
    if (order.getMovieShowTimeId() != null) {
      MovieShowTime showTime = movieShowTimeMapper.selectById(order.getMovieShowTimeId());
      theaterHallId = showTime != null ? showTime.getTheaterHallId() : null;
    }
    if (order.getMovieShowTimeId() == null || theaterHallId == null) {
      log.warn("支付被拒绝：订单缺少场次或影厅信息 orderNumber={}, orderId={}", orderNumber, orderId);
      throw new BusinessException(ResponseCode.ORDER_TIMEOUT, MessageKeys.Error.ORDER_OR_SEAT_EXPIRED);
    }
    List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
        order.getMovieShowTimeId(), theaterHallId, orderId);
    if (lockedSeats == null || lockedSeats.isEmpty()) {
      log.warn("支付被拒绝：座位锁定已过期 orderNumber={}, orderId={}", orderNumber, orderId);
      throw new BusinessException(ResponseCode.ORDER_TIMEOUT, MessageKeys.Error.ORDER_OR_SEAT_EXPIRED);
    }

    // 6. 防止重复支付：使用分布式锁，同一订单同时只能有一个支付请求
    String lockKey = RedisType.orderPaymentLock.getCode() + ":" + orderId;
    Boolean lockAcquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, String.valueOf(userId), paymentLockTimeoutSeconds, TimeUnit.SECONDS);
    if (Boolean.FALSE.equals(lockAcquired)) {
      throw new BusinessException(ResponseCode.PAYMENT_IN_PROGRESS, MessageKeys.Error.PAYMENT_IN_PROGRESS);
    }

    try {
      doPay(orderNumber, payId, order, orderId);
    } finally {
      redisTemplate.delete(lockKey);
    }
  }

  /**
   * 执行支付逻辑。调用方需已获取支付分布式锁。
   * 支付流程严格遵循：先存数据库，再存 Redis。
   */
  private void doPay(String orderNumber, Integer payId, MovieOrder order, Integer orderId) {
    // 双重检查：订单正在支付中则拒绝（防止异步处理期间重复请求）
    MovieOrder latestOrder = movieOrderMapper.selectById(orderId);
    if (latestOrder != null && Objects.equals(latestOrder.getPayState(), PayState.paying.getCode())) {
      throw new BusinessException(ResponseCode.PAYMENT_IN_PROGRESS, MessageKeys.Error.PAYMENT_IN_PROGRESS);
    }

    // 增加支付失败概率（30%失败率）：失败必须抛异常，接口返回错误，前端才能跳转失败页
    Random random = new Random();
    if (random.nextDouble() < 0.3) {
      String reason = "支付失败(30%模拟)";
      MovieOrder movieOrder = new MovieOrder();
      movieOrder.setId(orderId);
      movieOrder.setOrderState(OrderState.order_failed.getCode());
      movieOrder.setPayState(PayState.payment_failed.getCode());
      movieOrder.setFailureReason(reason);
      movieOrderMapper.updateById(movieOrder);
      clearLockedSeatsFromRedis(orderId, order.getMovieShowTimeId());
      refundOnPaymentFailure(orderId, order, reason);
      log.warn("支付失败: orderId={}", orderId);
      throw new BusinessException(ResponseCode.PAYMENT_FAILED, MessageKeys.App.Order.PAY_ERROR);
    }

    MovieOrder movieOrder = new MovieOrder();
    movieOrder.setId(orderId);

    // 支付流程：同步执行支付结果，接口等结果后再返回，确保失败时前端能跳转失败页
    movieOrder.setPayState(PayState.paying.getCode());
    movieOrder.setPayMethodId(payId);
    movieOrder.setPayTime(new Date());
    movieOrderMapper.updateById(movieOrder);

    // 增加支付失败概率（20%失败率）
    if (random.nextDouble() < 0.2) {
      String reason = "支付失败(20%模拟)";
      movieOrder.setOrderState(OrderState.order_failed.getCode());
      movieOrder.setPayState(PayState.payment_failed.getCode());
      movieOrder.setFailureReason(reason);
      movieOrderMapper.updateById(movieOrder);
      clearLockedSeatsFromRedis(orderId, order.getMovieShowTimeId());
      refundOnPaymentFailure(orderId, order, reason);
      log.warn("支付失败: orderId={}", orderId);
      throw new BusinessException(ResponseCode.PAYMENT_FAILED, MessageKeys.App.Order.PAY_ERROR);
    }

    // 校验座位是否仍有效
    Integer asyncTheaterHallId = null;
    MovieShowTime asyncShowTime = movieShowTimeMapper.selectById(order.getMovieShowTimeId());
    if (asyncShowTime != null) {
      asyncTheaterHallId = asyncShowTime.getTheaterHallId();
    }
    List<SelectSeat> lockedSeatsFromRedis = (asyncTheaterHallId != null)
        ? selectSeatService.getLockedSeatsByOrderIdFromRedis(
            order.getMovieShowTimeId(), asyncTheaterHallId, orderId)
        : Collections.emptyList();

    if (asyncTheaterHallId == null) {
      QueryWrapper<SelectSeat> lockedSeatsWrapper = new QueryWrapper<>();
      lockedSeatsWrapper.eq("movie_order_id", orderId)
          .eq("select_seat_state", SeatState.locked.getCode())
          .last("LIMIT 1");
      List<SelectSeat> dbSeats = selectSeatMapper.selectList(lockedSeatsWrapper);
      if (!dbSeats.isEmpty()) {
        asyncTheaterHallId = dbSeats.get(0).getTheaterHallId();
        lockedSeatsFromRedis = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            order.getMovieShowTimeId(), asyncTheaterHallId, orderId);
      }
    }

    if (lockedSeatsFromRedis.isEmpty()) {
      String reason = "座位锁定已过期";
      log.warn("支付时座位锁定已过期，回滚订单: orderId={}, orderNumber={}", orderId, orderNumber);
      movieOrder.setOrderState(OrderState.order_failed.getCode());
      movieOrder.setPayState(PayState.payment_failed.getCode());
      movieOrder.setFailureReason(reason);
      movieOrderMapper.updateById(movieOrder);
      refundOnPaymentFailure(orderId, order, reason);
      throw new BusinessException(ResponseCode.PAYMENT_FAILED, MessageKeys.App.Order.PAY_ERROR);
    }

    // 检查座位状态：过滤出仍然是 locked 且属于本订单的座位
    List<SelectSeat> validLockedSeats = lockedSeatsFromRedis.stream()
        .filter(seat -> seat.getSelectSeatState() != null &&
            seat.getSelectSeatState().intValue() == SeatState.locked.getCode() &&
            seat.getMovieOrderId() != null && seat.getMovieOrderId().equals(orderId))
        .collect(Collectors.toList());

    if (validLockedSeats.size() < lockedSeatsFromRedis.size()) {
      List<String> unavailableSeatNames = new ArrayList<>();
      lockedSeatsFromRedis.stream()
          .filter(seat -> !validLockedSeats.contains(seat))
          .forEach(seat -> unavailableSeatNames.add(seat.getSeatName() != null ? seat.getSeatName() : "座位ID:" + seat.getSeatId()));
      String reason = "部分座位不可用：" + String.join(", ", unavailableSeatNames);
      log.error("支付失败：部分座位不可用 orderId={}, orderNumber={}, 不可用={}", orderId, orderNumber, reason);
      movieOrder.setOrderState(OrderState.order_failed.getCode());
      movieOrder.setPayState(PayState.payment_failed.getCode());
      movieOrder.setFailureReason(reason);
      movieOrderMapper.updateById(movieOrder);
      refundOnPaymentFailure(orderId, order, reason);
      Map<Integer, String> lockedSeatIdToNameMap = lockedSeatsFromRedis.stream()
          .filter(seat -> seat.getSeatId() != null && seat.getSeatName() != null)
          .collect(Collectors.toMap(SelectSeat::getSeatId, SelectSeat::getSeatName, (a, b) -> a));
      if (!lockedSeatIdToNameMap.isEmpty()) {
        SelectSeat firstSeat = lockedSeatsFromRedis.get(0);
        selectSeatService.deleteSeatsFromRedis(firstSeat.getMovieShowTimeId(), firstSeat.getTheaterHallId(), lockedSeatIdToNameMap);
      }
      throw new BusinessException(ResponseCode.PAYMENT_FAILED, MessageKeys.App.Order.PAY_ERROR,
          Map.of("reason", reason));
    }

    // 先存数据库：更新订单为支付成功
    movieOrder.setOrderState(OrderState.order_succeed.getCode());
    movieOrder.setPayState(PayState.payment_successful.getCode());
    movieOrder.setPayTime(new Date());
    movieOrderMapper.updateById(movieOrder);

    // 创建 sold 状态座位并写 DB
    List<SelectSeat> soldSeats = validLockedSeats.stream().map(lockedSeat -> {
      SelectSeat soldSeat = new SelectSeat();
      soldSeat.setUserId(lockedSeat.getUserId());
      soldSeat.setMovieShowTimeId(lockedSeat.getMovieShowTimeId());
      soldSeat.setTheaterHallId(lockedSeat.getTheaterHallId());
      soldSeat.setX(lockedSeat.getX());
      soldSeat.setY(lockedSeat.getY());
      soldSeat.setSeatId(lockedSeat.getSeatId());
      soldSeat.setSeatName(lockedSeat.getSeatName());
      soldSeat.setMovieTicketTypeId(lockedSeat.getMovieTicketTypeId());
      soldSeat.setMovieOrderId(orderId);
      soldSeat.setSelectSeatState(SeatState.sold.getCode());
      return soldSeat;
    }).collect(Collectors.toList());
    selectSeatService.saveBatch(soldSeats);

    // 再存 Redis：locked 改为 sold
    for (SelectSeat lockedSeat : validLockedSeats) {
      lockedSeat.setSelectSeatState(SeatState.sold.getCode());
      selectSeatService.saveSeatToRedis(lockedSeat);
    }
    log.info("支付成功: orderId={}, seatCount={}", orderId, validLockedSeats.size());
  }
  @Transactional
  public void updateCancelOrTimeoutOrder(String orderNumber, String state, Integer currentUserId) {
    QueryWrapper<MovieOrder> orderWrapper = new QueryWrapper<>();
    orderWrapper.eq("order_number", orderNumber);
    MovieOrder movieOrder = movieOrderMapper.selectOne(orderWrapper);

    if (movieOrder == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }

    verifyOrderOwnership(movieOrder, currentUserId);
    verifyOrderStateFromCreated(movieOrder);

    Integer orderId = movieOrder.getId();
    Integer theaterHallId = null;
    if (movieOrder.getMovieShowTimeId() != null) {
      MovieShowTime st = movieShowTimeMapper.selectById(movieOrder.getMovieShowTimeId());
      theaterHallId = st != null ? st.getTheaterHallId() : null;
    }

    if ("cancel".equals(state)) {
      movieOrder.setOrderState(OrderState.canceled_order.getCode());
      movieOrder.setFailureReason("用户取消");
      movieOrderMapper.updateById(movieOrder);

      if (theaterHallId != null) {
        List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            movieOrder.getMovieShowTimeId(), theaterHallId, orderId);
        
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
            log.info("订单取消，已从 Redis 删除 locked 状态座位: orderNumber={}, orderId={}, seatCount={}", 
                orderNumber, orderId, lockedSeatIdToNameMap.size());
          }
        }
      }
    }
    
    if ("timeout".equals(state)) {
      movieOrder.setOrderState(OrderState.order_timeout.getCode());
      movieOrder.setFailureReason("订单超时");
      movieOrderMapper.updateById(movieOrder);

      if (theaterHallId != null) {
        List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            movieOrder.getMovieShowTimeId(), theaterHallId, orderId);
        
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
                orderNumber, orderId, lockedSeatIdToNameMap.size());
          }
        }
      }
    }
  }

  public List<MyTicketsResponse> getMyTickets(Integer userId) {
    // 第一步：获取用户的有效订单ID列表
    List<Integer> orderIds = movieOrderMapper.getUserValidOrderIds(userId);
    
    if (orderIds == null || orderIds.isEmpty()) {
      return Collections.emptyList();
    }
    
    // 第二步：批量获取订单基本信息
    List<MyTicketsResponse> tickets = movieOrderMapper.getMyTicketsByIds(orderIds);
    
    // 第三步：批量获取座位信息（按订单分组并去重，防止重复插入导致同一座位多条记录）
    Map<Integer, List<MovieOrderSeat>> seatMap = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds).stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId, Collectors.collectingAndThen(Collectors.toList(), MovieOrderService::dedupeSeatsBySeat)));
    
    // 第四步：设置座位信息到对应的订单（从 Redis 获取座位状态），并填充 specNames、dimensionType
    tickets.forEach(ticket -> {
      // 从数据库获取座位基本信息（已去重）
      List<MovieOrderSeat> seats = seatMap.getOrDefault(ticket.getId(), Collections.emptyList());
      ticket.setSeat(seats);
      // 规格名称：从聚合字符串拆分为列表
      ticket.setSpecNames(ticket.getSpecName() != null && !ticket.getSpecName().isEmpty()
          ? Arrays.asList(ticket.getSpecName().split("、"))
          : Collections.emptyList());
      // 放映类型为空时默认 1（2D）
      if (ticket.getDimensionType() == null) ticket.setDimensionType(1);

      // 如果订单有场次和影厅信息，从 Redis 获取选座状态和支付截止时间
      if (ticket.getMovieShowTimeId() != null && ticket.getTheaterHallId() != null) {
        // 从 Redis 获取该订单的 locked 状态座位
        List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            ticket.getMovieShowTimeId(), ticket.getTheaterHallId(), ticket.getId());
        
        // 如果 Redis 中有 locked 状态的座位，说明订单未支付，需要计算支付截止时间
        if (lockedSeats != null && !lockedSeats.isEmpty()) {
          // 检查是否有 locked 状态的座位
          boolean hasLockedSeats = lockedSeats.stream()
              .anyMatch(seat -> seat.getSelectSeatState() != null && 
                  seat.getSelectSeatState().intValue() == SeatState.locked.getCode());
          
          if (hasLockedSeats && ticket.getOrderTime() != null) {
            // 从订单创建时间计算支付截止时间
            Date payDeadline = new Date(ticket.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
            ticket.setPayDeadline(payDeadline);
          }
        }
      }
    });
    
    return tickets;
  }

  /** 按座位去重（同一订单下 seatName 或 x_y 相同只保留一条），防止 DB 重复插入导致展示重复 */
  public static List<MovieOrderSeat> dedupeSeatsBySeat(List<MovieOrderSeat> list) {
    if (list == null || list.isEmpty()) return list;
    return list.stream().collect(Collectors.toMap(
      s -> (s.getSeatName() != null && !s.getSeatName().isEmpty())
          ? s.getSeatName()
          : "xy_" + (s.getSeatX() != null ? s.getSeatX() : "") + "_" + (s.getSeatY() != null ? s.getSeatY() : ""),
      s -> s, (a, b) -> a, LinkedHashMap::new)).values().stream().collect(Collectors.toList());
  }

  public IPage<MyTicketsResponse> getMyTicketsPage(MyTicketsQuery query) {
    // 设置用户ID
    query.setUserId(StpUtil.getLoginIdAsInt());
    
    // 第一步：获取用户的有效订单ID列表（带分页）
    Page<Integer> orderIdPage = new Page<>(query.getPage(), query.getPageSize());
    IPage<Integer> orderIdResult = movieOrderMapper.getUserValidOrderIdsPage(query, orderIdPage);
    
    if (orderIdResult.getRecords() == null || orderIdResult.getRecords().isEmpty()) {
      // 返回空的分页结果
      Page<MyTicketsResponse> emptyPage = new Page<>(query.getPage(), query.getPageSize());
      emptyPage.setTotal(0);
      return emptyPage;
    }
    
    List<Integer> orderIds = orderIdResult.getRecords();
    
    // 第二步：批量获取订单基本信息
    List<MyTicketsResponse> tickets = movieOrderMapper.getMyTicketsByIds(orderIds);
    
    // 第三步：批量获取座位信息（按订单分组并去重，防止重复插入导致同一座位多条记录）
    Map<Integer, List<MovieOrderSeat>> seatMap = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds).stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId, Collectors.collectingAndThen(Collectors.toList(), MovieOrderService::dedupeSeatsBySeat)));
    
    // 第四步：设置座位信息到对应的订单（从 Redis 获取座位状态），并填充 specNames、dimensionType
    tickets.forEach(ticket -> {
      // 从数据库获取座位基本信息（已去重）
      List<MovieOrderSeat> seats = seatMap.getOrDefault(ticket.getId(), Collections.emptyList());
      ticket.setSeat(seats);
      // 规格名称：从聚合字符串拆分为列表
      ticket.setSpecNames(ticket.getSpecName() != null && !ticket.getSpecName().isEmpty()
          ? Arrays.asList(ticket.getSpecName().split("、"))
          : Collections.emptyList());
      // 放映类型为空时默认 1（2D）
      if (ticket.getDimensionType() == null) ticket.setDimensionType(1);

      // 如果订单有场次和影厅信息，从 Redis 获取选座状态和支付截止时间
      if (ticket.getMovieShowTimeId() != null && ticket.getTheaterHallId() != null) {
        // 从 Redis 获取该订单的 locked 状态座位
        List<SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
            ticket.getMovieShowTimeId(), ticket.getTheaterHallId(), ticket.getId());
        
        // 如果 Redis 中有 locked 状态的座位，说明订单未支付，需要计算支付截止时间
        if (lockedSeats != null && !lockedSeats.isEmpty()) {
          // 检查是否有 locked 状态的座位
          boolean hasLockedSeats = lockedSeats.stream()
              .anyMatch(seat -> seat.getSelectSeatState() != null && 
                  seat.getSelectSeatState().intValue() == SeatState.locked.getCode());
          
          if (hasLockedSeats && ticket.getOrderTime() != null) {
            // 从订单创建时间计算支付截止时间
            Date payDeadline = new Date(ticket.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
            ticket.setPayDeadline(payDeadline);
          }
        }
      }
    });
    
    // 第五步：构造分页结果
    Page<MyTicketsResponse> result = new Page<>(query.getPage(), query.getPageSize());
    result.setRecords(tickets);
    result.setTotal(orderIdResult.getTotal());
    
    return result;
  }
}
