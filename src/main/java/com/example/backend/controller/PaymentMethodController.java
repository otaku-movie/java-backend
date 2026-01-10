package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Brand;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.BrandMapper;
import com.example.backend.mapper.PaymentMethodMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaymentMethodController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private PaymentMethodMapper paymentMethodMapper;

  @GetMapping(ApiPaths.Common.PaymentMethod.LIST)
  public RestBean<List<PaymentMethod>> list() {
    QueryWrapper queryWrapper = new QueryWrapper();

    List<PaymentMethod> list = paymentMethodMapper.selectList(queryWrapper);

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
}
