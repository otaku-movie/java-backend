package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Refund;
import com.example.backend.entity.RestBean;
import com.example.backend.query.refund.RefundListQuery;
import com.example.backend.query.refund.RefundProcessQuery;
import com.example.backend.service.RefundService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class RefundApplyQuery {
  @NotBlank(message = "订单号不能为空")
  private String orderNumber;
  private String reason;
}

@RestController
public class RefundController {

  @Autowired
  private RefundService refundService;

  /** 按订单号查退款列表 */
  @SaCheckLogin
  @GetMapping(ApiPaths.Common.Refund.LIST)
  public RestBean<List<Refund>> list(@RequestParam("orderNumber") String orderNumber) {
    List<Refund> list = refundService.listByOrderNumber(orderNumber, StpUtil.getLoginIdAsInt());
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /** 我的退款列表（分页） */
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Refund.MY_LIST)
  public RestBean<List<Refund>> myList(@RequestBody @Valid RefundListQuery query) {
    IPage<Refund> page = refundService.myList(query, StpUtil.getLoginIdAsInt());
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  /** 退款详情 */
  @SaCheckLogin
  @GetMapping(ApiPaths.Common.Refund.DETAIL)
  public RestBean<Refund> detail(@RequestParam("id") Integer id) {
    Refund refund = refundService.getDetail(id, StpUtil.getLoginIdAsInt());
    return RestBean.success(refund, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /** 申请退款 */
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Refund.APPLY)
  public RestBean<Refund> apply(@RequestBody @Validated RefundApplyQuery query) {
    Refund refund = refundService.apply(query.getOrderNumber(), query.getReason());
    return RestBean.success(refund, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  /** 取消退款申请 */
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.Refund.CANCEL)
  public RestBean<?> cancelApply(@RequestParam("id") Integer id) {
    refundService.cancelApply(id, StpUtil.getLoginIdAsInt());
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  /** 管理端：退款列表（分页+筛选） */
  @SaCheckLogin
  @CheckPermission(code = "refund.list")
  @PostMapping(ApiPaths.Admin.Refund.LIST)
  public RestBean<List<Refund>> adminList(@RequestBody @Valid RefundListQuery query) {
    IPage<Refund> page = refundService.adminList(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  /** 管理端：处理退款（同意/拒绝） */
  @SaCheckLogin
  @CheckPermission(code = "refund.process")
  @PostMapping(ApiPaths.Admin.Refund.PROCESS)
  public RestBean<Refund> process(@RequestBody @Validated RefundProcessQuery query) {
    Refund refund = refundService.process(
        query.getId(), query.getApproved(), query.getRejectReason(), StpUtil.getLoginIdAsInt());
    return RestBean.success(refund, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
}
