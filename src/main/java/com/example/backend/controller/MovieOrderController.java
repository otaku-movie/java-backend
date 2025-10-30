package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieOrderMapper;
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
  Integer orderId;
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


  @SaCheckLogin
  @PostMapping("/api/movieOrder/create")
  public RestBean<MovieOrder> createOrder(@RequestBody @Validated MovieOrderSaveQuery query) throws Exception {
    MovieOrder order = movieOrderService.createOrder(query);

    return RestBean.success(order, MessageUtils.getMessage("success.save"));
  }
//  @SaCheckLogin
  @GetMapping("/api/movieOrder/detail")
  public RestBean<OrderListResponse> OrderDetail( @RequestParam("id") Integer id) {
    OrderListResponse order = movieOrderMapper.orderDetail(id);
    
    if (order == null) {
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.order.notFound"));
    }

    List<MovieOrderSeat> seats = movieOrderMapper.getMovieOrderSeatListByOrderIds(List.of(id));
    order.setSeat(seats != null ? seats : Collections.emptyList());

    return RestBean.success(order, MessageUtils.getMessage("success.get"));
  }
  @PostMapping("/api/admin/movieOrder/list")
  public RestBean<List<OrderListResponse>> orderList(@RequestBody MovieOrderListQuery query) {
    // 初始化分页对象
    Page<OrderListResponse> page = new Page<>(query.getPage(), query.getPageSize());

    // 查询订单列表
    IPage<OrderListResponse> list = movieOrderMapper.orderList(query, page);

    // 提取订单 ID 列表
    List<Integer> orderIds = list.getRecords().stream()
      .map(OrderListResponse::getId)
      .toList();

    // 批量查询所有订单对应的座位信息，并按订单 ID 分组
    Map<Integer, List<MovieOrderSeat>> seatMap = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds).stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId));

    // 设置座位信息
    List<OrderListResponse> result = list.getRecords().stream()
      .peek(item -> item.setSeat(seatMap.getOrDefault(item.getId(), Collections.emptyList())))
      .toList();

    // 返回分页结果
    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
//  @SaCheckLogin
//  @CheckPermission(code ="movieOrder.updateOrderState")
//  @PostMapping("/api/admin/movieOrder/updateOrderState")
//  public RestBean<Null> updateOrderState(@RequestBody UpdateOrderStateQuery query) {
//    movieOrderService.updateOrderState(query);
//
//    return RestBean.success(null, MessageUtils.getMessage("success.save"));
//  }
  @SaCheckLogin
  @CheckPermission(code ="movieOrder.remove")
  @DeleteMapping("/api/admin/movieOrder/remove")
  public RestBean<Null> removeOrder(@RequestParam("id") Integer id) {
    movieOrderMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @PostMapping("/api/movieOrder/pay")
  public RestBean<Null> pay(@RequestBody MovieOrderPayQuery query) {
    MovieOrder movieOrder =  movieOrderMapper.selectById(query.getOrderId());

    if (movieOrder.getOrderState() == OrderState.order_created.getCode()) {
      movieOrderService.pay(query.getOrderId(), query.getPayId());

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } else {
      return  RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.order.payError"));
    }
  }

  /**
   * 信用卡支付接口
   */
  @SaCheckLogin
  @PostMapping("/movieOrder/pay")
  public RestBean<Null> payCreditCard(@RequestBody @Validated CreditCardPayQuery query) {
    try {
      MovieOrder movieOrder = movieOrderMapper.selectById(query.getOrderId());
      
      if (movieOrder == null) {
        return RestBean.error(ResponseCode.ERROR.getCode(), "订单不存在");
      }

      if (movieOrder.getOrderState() != OrderState.order_created.getCode()) {
        return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.order.payError"));
      }

      // 处理信用卡支付
      boolean paymentResult = paymentService.processCreditCardPayment(query);
      
      if (paymentResult) {
        // 支付成功，更新订单状态
        movieOrderService.pay(query.getOrderId(), null);
        return RestBean.success(null, "支付成功");
      } else {
        return RestBean.error(ResponseCode.ERROR.getCode(), "支付失败");
      }
    } catch (Exception e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
    }
  }

  @SaCheckLogin
  @PostMapping("/api/movieOrder/cancel")
  public RestBean<Null> cancelOrder(@RequestBody @Validated CancelOrderQuery query) {
    try {
      movieOrderService.updateCancelOrTimeoutOrder(query.getOrderId(), "cancel");
      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } catch (Exception e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
    }
  }
  @SaCheckLogin
  @PostMapping("/api/movieOrder/timeout")
  public RestBean<Null> timeoutOrder(@RequestBody @Validated CancelOrderQuery query) {
    try {
      movieOrderService.updateCancelOrTimeoutOrder(query.getOrderId(), "timeout");
      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } catch (Exception e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
    }
  }

  @SaCheckLogin
  @PostMapping("/api/movieOrder/myTickets")
  public RestBean<List<MyTicketsResponse>> getMyTickets(@RequestBody @Validated MyTicketsQuery query) {
    IPage<MyTicketsResponse> result = movieOrderService.getMyTicketsPage(query);
    
    return RestBean.success(result.getRecords(), query.getPage(), result.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movieOrder/generatorQRcode")
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
