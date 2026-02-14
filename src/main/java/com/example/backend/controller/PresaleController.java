package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.query.presale.PresaleListQuery;
import com.example.backend.query.presale.PresaleSaveQuery;
import com.example.backend.response.presale.PresaleDetailResponse;
import com.example.backend.response.presale.PresaleListItemResponse;
import com.example.backend.service.PresaleService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class PresaleController {

  @Autowired
  private PresaleService presaleService;

  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.Presale.DETAIL)
  public RestBean<PresaleDetailResponse> detail(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(
        ResponseCode.PARAMETER_ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR)
      );
    }
    PresaleDetailResponse response = presaleService.getDetail(id);
    if (response == null) {
      return RestBean.error(
        ResponseCode.PARAMETER_ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR)
      );
    }
    return RestBean.success(response, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Presale.LIST)
  public RestBean<List<PresaleListItemResponse>> list(@RequestBody(required = false) PresaleListQuery query) {
    if (query == null) {
      query = new PresaleListQuery();
    }
    var page = presaleService.list(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @CheckPermission(code = "presale.save")
  @PostMapping(ApiPaths.Admin.Presale.SAVE)
  public RestBean<String> save(@Valid @RequestBody PresaleSaveQuery query) {
    presaleService.save(query);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "presale.remove")
  @DeleteMapping(ApiPaths.Admin.Presale.REMOVE)
  public RestBean<String> remove(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(
        ResponseCode.PARAMETER_ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR)
      );
    }
    presaleService.remove(id);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
  }
}
