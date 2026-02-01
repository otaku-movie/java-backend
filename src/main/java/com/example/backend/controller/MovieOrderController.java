package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.query.order.CancelOrderQuery;
import com.example.backend.query.order.MyTicketsQuery;
import com.example.backend.query.order.MovieOrderListQuery;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.response.order.MovieOrderSeat;
import com.example.backend.response.order.MyTicketsResponse;
import com.example.backend.response.order.OrderListResponse;
import com.example.backend.service.MovieOrderService;
import com.example.backend.query.CreditCardPayQuery;
import com.example.backend.service.PaymentService;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Data class MovieOrderPayQuery {
  String orderNumber;  // 订单号
  Integer payId;
}

@RestController
public class MovieOrderController {
  @Autowired
  MovieOrderService movieOrderService;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @Autowired
  PaymentService paymentService;

  @Autowired
  private MessageUtils messageUtils;
  
  @Autowired
  private com.example.backend.service.SelectSeatService selectSeatService;

  @Autowired
  private MovieTicketTypeMapper movieTicketTypeMapper;
  
  @org.springframework.beans.factory.annotation.Value("${order.payment-timeout:900}")
  private long paymentTimeoutSeconds;

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.CREATE)
  public RestBean<MovieOrder> createOrder(@RequestBody @Validated MovieOrderSaveQuery query) throws Exception {
    MovieOrder order = movieOrderService.createOrder(query);

    return RestBean.success(order, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
  @SaCheckLogin
  @GetMapping(ApiPaths.Common.Order.DETAIL)
  public RestBean<OrderListResponse> OrderDetail(@RequestParam("orderNumber") String orderNumber) {
    QueryWrapper<MovieOrder> qw = new QueryWrapper<>();
    qw.eq("order_number", orderNumber).last("LIMIT 1");
    MovieOrder mo = movieOrderMapper.selectOne(qw);
    Integer orderId = mo.getId();
    movieOrderService.verifyOrderAccess(orderId, StpUtil.getLoginIdAsInt());
    OrderListResponse order = movieOrderMapper.orderDetail(orderId);

    if (order == null) {
      return RestBean.error(ResponseCode.ORDER_NOT_FOUND.getCode(), MessageUtils.getMessage(MessageKeys.Error.ORDER_NOT_FOUND));
    }

    // 从数据库获取座位基本信息（已支付订单的座位在 DB；未支付订单的座位只在 Redis），按座位去重
    List<MovieOrderSeat> seats = MovieOrderService.dedupeSeatsBySeat(movieOrderMapper.getMovieOrderSeatListByOrderIds(List.of(orderId)));
    
    // 如果订单有场次和影厅信息，从 Redis 获取选座状态
    if (order.getMovieShowTimeId() != null && order.getTheaterHallId() != null) {
      // 从 Redis 获取该订单的 locked 状态座位
      List<com.example.backend.entity.SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
          order.getMovieShowTimeId(), order.getTheaterHallId(), orderId);
      
      // 如果 Redis 中有 locked 状态的座位，说明订单未支付，需要计算支付截止时间
      if (lockedSeats != null && !lockedSeats.isEmpty()) {
        // 检查是否有 locked 状态的座位
        boolean hasLockedSeats = lockedSeats.stream()
            .anyMatch(seat -> seat.getSelectSeatState() != null && 
                seat.getSelectSeatState().intValue() == com.example.backend.enumerate.SeatState.locked.getCode());
        
        if (hasLockedSeats && order.getOrderTime() != null) {
          // 从订单创建时间计算支付截止时间
          Date payDeadline = new Date(order.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
          order.setPayDeadline(payDeadline);
        }
        // 未支付订单座位只存在 Redis：DB 无座位时用 Redis 数据补全
        if (seats == null || seats.isEmpty()) {
          seats = lockedSeats.stream().map(locked -> {
            MovieOrderSeat s = new MovieOrderSeat();
            s.setSeatX(locked.getX());
            s.setSeatY(locked.getY());
            s.setSeatName(locked.getSeatName());
            if (locked.getMovieTicketTypeId() != null) {
              MovieTicketType tt = movieTicketTypeMapper.selectById(locked.getMovieTicketTypeId());
              s.setMovieTicketTypeName(tt != null ? tt.getName() : null);
            }
            return s;
          }).collect(Collectors.toList());
        }
      }
    } else {
      // 如果没有场次和影厅信息，使用原来的逻辑计算支付截止时间
      if (order.getOrderState() != null && order.getOrderState().equals(com.example.backend.enumerate.OrderState.order_created.getCode()) 
          && order.getOrderTime() != null) {
        Date payDeadline = new Date(order.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
        order.setPayDeadline(payDeadline);
      }
    }
    
    order.setSeat(seats != null ? seats : Collections.emptyList());

    // 规格名称：从聚合字符串拆分为列表
    order.setSpecNames(order.getSpecName() != null && !order.getSpecName().isEmpty()
        ? Arrays.asList(order.getSpecName().split("、"))
        : Collections.emptyList());
    // 放映类型为空时默认 1（2D）
    order.setDimensionType(order.getDimensionType() != null ? order.getDimensionType() : 1);

    return RestBean.success(order, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @PostMapping(ApiPaths.Admin.Order.LIST)
  public RestBean<List<OrderListResponse>> orderList(@RequestBody MovieOrderListQuery query) {
    // 初始化分页对象
    Page<OrderListResponse> page = new Page<>(query.getPage(), query.getPageSize());

    // 查询订单列表
    IPage<OrderListResponse> list = movieOrderMapper.orderList(query, page);

    // 提取订单 ID 列表
    List<Integer> orderIds = list.getRecords().stream()
      .map(OrderListResponse::getId)
      .toList();

    // 批量查询所有订单对应的座位信息，按订单 ID 分组并按座位去重
    Map<Integer, List<MovieOrderSeat>> seatMap = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds).stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId, Collectors.collectingAndThen(Collectors.toList(), MovieOrderService::dedupeSeatsBySeat)));

    // 设置座位信息和支付截止时间（从 Redis 获取座位状态）
    List<OrderListResponse> result = list.getRecords().stream()
      .peek(item -> {
        // 从数据库获取座位基本信息
        item.setSeat(seatMap.getOrDefault(item.getId(), Collections.emptyList()));
        // 规格名称：从聚合字符串拆分为列表
        item.setSpecNames(item.getSpecName() != null && !item.getSpecName().isEmpty()
            ? Arrays.asList(item.getSpecName().split("、"))
            : Collections.emptyList());
        // 放映类型为空时默认 1（2D）
        if (item.getDimensionType() == null) item.setDimensionType(1);
        
        // 如果订单有场次和影厅信息，从 Redis 获取选座状态和支付截止时间
        if (item.getMovieShowTimeId() != null && item.getTheaterHallId() != null) {
          // 从 Redis 获取该订单的 locked 状态座位
          List<com.example.backend.entity.SelectSeat> lockedSeats = selectSeatService.getLockedSeatsByOrderIdFromRedis(
              item.getMovieShowTimeId(), item.getTheaterHallId(), item.getId());
          
          // 如果 Redis 中有 locked 状态的座位，说明订单未支付，需要计算支付截止时间
          if (lockedSeats != null && !lockedSeats.isEmpty()) {
            // 检查是否有 locked 状态的座位
            boolean hasLockedSeats = lockedSeats.stream()
                .anyMatch(seat -> seat.getSelectSeatState() != null && 
                    seat.getSelectSeatState().intValue() == com.example.backend.enumerate.SeatState.locked.getCode());
            
            if (hasLockedSeats && item.getOrderTime() != null) {
              // 从订单创建时间计算支付截止时间
              Date payDeadline = new Date(item.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
              item.setPayDeadline(payDeadline);
            }
          }
        } else {
          // 如果没有场次和影厅信息，使用原来的逻辑计算支付截止时间
          if (item.getOrderState() != null && item.getOrderState().equals(com.example.backend.enumerate.OrderState.order_created.getCode()) 
              && item.getOrderTime() != null) {
            Date payDeadline = new Date(item.getOrderTime().getTime() + paymentTimeoutSeconds * 1000L);
            item.setPayDeadline(payDeadline);
          }
        }
      })
      .toList();

    // 返回分页结果
    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @CheckPermission(code ="movieOrder.remove")
  @DeleteMapping(ApiPaths.Admin.Order.REMOVE)
  public RestBean<Null> removeOrder(@RequestParam("id") Integer id) {
    movieOrderMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.PAY)
  public RestBean<Null> pay(@RequestBody MovieOrderPayQuery query) {
    QueryWrapper<MovieOrder> orderWrapper = new QueryWrapper<>();
    orderWrapper.eq("order_number", query.getOrderNumber());
    MovieOrder movieOrder = movieOrderMapper.selectOne(orderWrapper);

    if (movieOrder == null) {
      return RestBean.error(ResponseCode.ORDER_NOT_FOUND.getCode(), MessageUtils.getMessage(MessageKeys.Error.ORDER_NOT_FOUND));
    }

    // 已支付订单幂等返回成功
    if (Objects.equals(movieOrder.getOrderState(), OrderState.order_succeed.getCode())) {
      return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    }
    // 订单已超时，拒绝支付
    if (Objects.equals(movieOrder.getOrderState(), OrderState.order_timeout.getCode())) {
      return RestBean.error(ResponseCode.ORDER_TIMEOUT.getCode(),
          messageUtils.getMessage(MessageKeys.Error.ORDER_OR_SEAT_EXPIRED));
    }
    if (Objects.equals(movieOrder.getOrderState(), OrderState.order_created.getCode())) {
      movieOrderService.pay(query.getOrderNumber(), query.getPayId());
      return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    }
    return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.App.Order.PAY_ERROR));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.CANCEL)
  public RestBean<Null> cancelOrder(@RequestBody @Validated CancelOrderQuery query) {
    movieOrderService.updateCancelOrTimeoutOrder(query.getOrderNumber(), "cancel", StpUtil.getLoginIdAsInt());
    return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.TIMEOUT)
  public RestBean<Null> timeoutOrder(@RequestBody @Validated CancelOrderQuery query) {
    movieOrderService.updateCancelOrTimeoutOrder(query.getOrderNumber(), "timeout", StpUtil.getLoginIdAsInt());
    return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.MY_TICKETS)
  public RestBean<List<MyTicketsResponse>> getMyTickets(@RequestBody @Validated MyTicketsQuery query) {
    IPage<MyTicketsResponse> result = movieOrderService.getMyTicketsPage(query);
    
    return RestBean.success(result.getRecords(), query.getPage(), result.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.Order.GENERATOR_QR_CODE)
  public ResponseEntity<ByteArrayResource> generatorQRcode() {
    QrConfig config = new QrConfig(300, 300);
    QrCodeUtil.generate("https://www.google.com/", config);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    QrCodeUtil.generate("https://www.google.com/", config, "png", stream);

    // 将字节数组输出流转换为字节数组资源
    ByteArrayResource resource = new ByteArrayResource(stream.toByteArray());

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qrcode.png")
      .contentType(MediaType.IMAGE_PNG)
      .contentLength(resource.contentLength())
      .body(resource);
  }
}
