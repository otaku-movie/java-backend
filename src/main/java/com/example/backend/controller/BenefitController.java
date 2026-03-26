package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.query.benefit.BenefitFeedbackListQuery;
import com.example.backend.query.benefit.BenefitListQuery;
import com.example.backend.query.benefit.BenefitStockListQuery;
import com.example.backend.query.benefit.BenefitStockSaveQuery;
import com.example.backend.response.benefit.BenefitDetailResponse;
import com.example.backend.response.benefit.BenefitFeedbackListItemResponse;
import com.example.backend.response.benefit.BenefitListItemResponse;
import com.example.backend.response.benefit.BenefitStockListItemResponse;
import com.example.backend.response.benefit.CinemaBenefitSummaryResponse;
import com.example.backend.service.BenefitService;
import com.example.backend.utils.MessageUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 入场者特典管理（设计 6）
 */
@RestController
@Validated
public class BenefitController {

  @Autowired
  private BenefitService benefitService;

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Benefit.LIST)
  public RestBean<List<BenefitListItemResponse>> list(@RequestBody(required = false) BenefitListQuery query) {
    if (query == null) query = new BenefitListQuery();
    IPage<BenefitListItemResponse> page = benefitService.listBenefitForAdmin(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.Benefit.DETAIL)
  public RestBean<BenefitDetailResponse> detail(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    BenefitDetailResponse response = benefitService.getBenefitDetail(id);
    if (response == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    return RestBean.success(response, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "benefit.save")
  @PostMapping(ApiPaths.Admin.Benefit.SAVE)
  public RestBean<Integer> save(@Valid @RequestBody com.example.backend.query.benefit.BenefitSaveQuery query) {
    Integer id = benefitService.saveBenefit(query);
    return RestBean.success(id, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "benefit.remove")
  @DeleteMapping(ApiPaths.Admin.Benefit.REMOVE)
  public RestBean<String> remove(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    benefitService.removeBenefit(id);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.REMOVE_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Cinema.Benefit.STOCK_LIST)
  public RestBean<List<BenefitStockListItemResponse>> stockList(@RequestBody(required = false) BenefitStockListQuery query) {
    if (query == null) query = new BenefitStockListQuery();
    IPage<BenefitStockListItemResponse> page = benefitService.listStockForAdmin(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  @SaCheckLogin
  @CheckPermission(code = "benefit.stock.save")
  @PostMapping(ApiPaths.Admin.Cinema.Benefit.STOCK_SAVE)
  public RestBean<String> stockSave(@Valid @RequestBody BenefitStockSaveQuery query) {
    benefitService.saveBenefitStock(query);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Cinema.Benefit.FEEDBACK_LIST)
  public RestBean<List<BenefitFeedbackListItemResponse>> feedbackList(@RequestBody(required = false) BenefitFeedbackListQuery query) {
    if (query == null) query = new BenefitFeedbackListQuery();
    IPage<BenefitFeedbackListItemResponse> page = benefitService.listFeedbackForAdmin(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  /** 按电影查询影院特典汇总：每家影院特典数量、剩余、用户反馈数（供后台影院/运营查看） */
  @SaCheckLogin
  /** 影院维度特典汇总：按电影查各家影院特典数量、剩余、反馈数 */
  @GetMapping(ApiPaths.Admin.Cinema.Benefit.SUMMARY)
  public RestBean<List<CinemaBenefitSummaryResponse>> cinemaSummary(@RequestParam Integer movieId) {
    if (movieId == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    List<CinemaBenefitSummaryResponse> list = benefitService.listCinemaBenefitSummaryByMovie(movieId);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
}
