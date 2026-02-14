package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.query.promotion.PromotionListQuery;
import com.example.backend.query.promotion.PromotionSaveQuery;
import com.example.backend.response.promotion.PromotionDetailResponse;
import com.example.backend.response.promotion.PromotionListItemResponse;
import com.example.backend.service.PromotionService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PromotionController {

  @Autowired
  private PromotionService promotionService;

  /**
   * App 端：按影院获取价格策略列表，按优先级升序返回，供前端按顺序计算价格。
   * 无需登录即可调用。
   */
  @GetMapping(ApiPaths.Common.Cinema.PROMOTIONS)
  public RestBean<java.util.List<PromotionListItemResponse>> listByCinema(@RequestParam Integer cinemaId) {
    if (cinemaId == null) {
      return RestBean.error(
        ResponseCode.PARAMETER_ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR)
      );
    }
    java.util.List<PromotionListItemResponse> list = promotionService.listPromotionsByCinema(cinemaId);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @SaCheckLogin
  // @CheckPermission(code = "promotion.detail")
  @GetMapping(ApiPaths.Admin.Promotion.DETAIL)
  public RestBean<PromotionDetailResponse> detail(@RequestParam Integer cinemaId) {
    if (cinemaId == null) {
      return RestBean.error(
        ResponseCode.PARAMETER_ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR)
      );
    }
    PromotionDetailResponse response = promotionService.getPromotionDetail(cinemaId, null);
    return RestBean.<PromotionDetailResponse>success(response, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @SaCheckLogin
  // @CheckPermission(code = "promotion.list")
  @PostMapping(ApiPaths.Admin.Promotion.LIST)
  public RestBean<java.util.List<PromotionListItemResponse>> list(@RequestBody(required = false) PromotionListQuery query) {
    if (query == null) {
      query = new PromotionListQuery();
    }
    var page = promotionService.listPromotions(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @CheckPermission(code = "promotion.save")
  @PostMapping(ApiPaths.Admin.Promotion.SAVE)
  public RestBean<String> save(@Valid @RequestBody PromotionSaveQuery query) {
    promotionService.savePromotion(query);
    return RestBean.<String>success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "promotion.remove")
  @DeleteMapping(ApiPaths.Admin.Promotion.REMOVE)
  public RestBean<String> remove(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    promotionService.deletePromotion(id);
    return RestBean.<String>success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
  }
}
