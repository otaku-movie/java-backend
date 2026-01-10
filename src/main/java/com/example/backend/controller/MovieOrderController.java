package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
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

  @Autowired
  private MessageUtils messageUtils;

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.CREATE)
  public RestBean<MovieOrder> createOrder(@RequestBody @Validated MovieOrderSaveQuery query) throws Exception {
    MovieOrder order = movieOrderService.createOrder(query);

    return RestBean.success(order, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
//  @SaCheckLogin
  @GetMapping(ApiPaths.Common.Order.DETAIL)
  public RestBean<OrderListResponse> OrderDetail( @RequestParam("id") Integer id) {
    OrderListResponse order = movieOrderMapper.orderDetail(id);
    
    if (order == null) {
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.USER_NOT_FOUND));
    }

    List<MovieOrderSeat> seats = movieOrderMapper.getMovieOrderSeatListByOrderIds(List.of(id));
    order.setSeat(seats != null ? seats : Collections.emptyList());

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
    MovieOrder movieOrder =  movieOrderMapper.selectById(query.getOrderId());

    if (movieOrder.getOrderState() == OrderState.order_created.getCode()) {
      movieOrderService.pay(query.getOrderId(), query.getPayId());

      return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    } else {
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.App.Order.PAY_ERROR));
    }
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.CANCEL)
  public RestBean<Null> cancelOrder(@RequestBody @Validated CancelOrderQuery query) {
    try {
      movieOrderService.updateCancelOrTimeoutOrder(query.getOrderId(), "cancel");
      return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    } catch (Exception e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
    }
  }
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Order.TIMEOUT)
  public RestBean<Null> timeoutOrder(@RequestBody @Validated CancelOrderQuery query) {
    try {
      movieOrderService.updateCancelOrTimeoutOrder(query.getOrderId(), "timeout");
      return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
    } catch (Exception e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
    }
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
