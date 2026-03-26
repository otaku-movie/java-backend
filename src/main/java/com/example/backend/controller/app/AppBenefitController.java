package com.example.backend.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.query.benefit.BenefitFeedbackSubmitQuery;
import com.example.backend.response.benefit.BenefitDetailResponse;
import com.example.backend.service.BenefitService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App 端 - 入场者特典（C 端查看某电影的特典列表；用户反馈需登录）
 */
@RestController
@Validated
public class AppBenefitController {

  @Autowired
  private BenefitService benefitService;

  @GetMapping(ApiPaths.App.Benefit.LIST)
  public RestBean<List<BenefitDetailResponse>> list(@RequestParam Integer movieId) {
    if (movieId == null) {
      return RestBean.success(List.of(), MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
    List<BenefitDetailResponse> list = benefitService.listBenefitDetailByMovie(movieId);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /** 用户反馈：当前影院该特典物料已领完等（需登录） */
  @SaCheckLogin
  @PostMapping(ApiPaths.App.Benefit.FEEDBACK_SUBMIT)
  public RestBean<String> submitFeedback(@Valid @RequestBody BenefitFeedbackSubmitQuery query) {
    Integer userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsInt();
    benefitService.submitFeedback(
      userId,
      query.getCinemaId(),
      query.getBenefitId(),
      query.getFeedbackType() != null ? query.getFeedbackType() : 1);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
}
