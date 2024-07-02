package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
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
