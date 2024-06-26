package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.query.order.MovieOrderListQuery;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.query.order.UpdateOrderStateQuery;
import com.example.backend.response.order.OrderListResponse;
import com.example.backend.service.MovieOrderService;
import com.example.backend.utils.MessageUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MovieOrderController {
  @Autowired
  MovieOrderService movieOrderService;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @SaCheckLogin
  @PostMapping("/api/movieOrder/create")
  public RestBean<Null> createOrder(@RequestBody @Validated MovieOrderSaveQuery query) {
    movieOrderService.createOrder(query);

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @PostMapping("/api/admin/movieOrder/list")
  public RestBean<List<OrderListResponse>> orderList(@RequestBody MovieOrderListQuery query) {
    Page<OrderListResponse> page = new Page<>(query.getPage(), query.getPageSize());
    IPage<OrderListResponse> list = movieOrderMapper.orderList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @SaCheckLogin
  @CheckPermission(code ="movieOrder.updateOrderState")
  @PostMapping("/api/admin/movieOrder/updateOrderState")
  public RestBean<Null> updateOrderState(@RequestBody UpdateOrderStateQuery query) {
    movieOrderService.updateOrderState(query);

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @SaCheckLogin
  @CheckPermission(code ="movieOrder.remove")
  @DeleteMapping("/api/admin/movieOrder/remove")
  public RestBean<Null> removeOrder(@RequestParam("id") Integer id) {
    movieOrderMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
}
