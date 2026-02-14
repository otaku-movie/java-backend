package com.example.backend.controller.app;

import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.response.presale.PresaleDetailResponse;
import com.example.backend.service.PresaleService;
import com.example.backend.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端预售券接口（C 端查看预售券详情，无需登录）
 */
@RestController
public class AppPresaleController {

  @Autowired
  private PresaleService presaleService;

  @GetMapping(ApiPaths.App.Presale.DETAIL)
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
    return RestBean.success(response, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
}
