package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.Refund;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.RefundApplyStatus;
import com.example.backend.enumerate.RefundState;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.RefundMapper;
import com.example.backend.constants.MessageKeys;
import com.example.backend.query.refund.RefundListQuery;
import com.example.backend.service.PaymentService;
import com.example.backend.enumerate.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RefundService extends ServiceImpl<RefundMapper, Refund> {

  @Autowired
  private RefundMapper refundMapper;

  @Autowired
  private MovieOrderMapper movieOrderMapper;

  @Autowired
  private PaymentService paymentService;

  /**
   * 按订单号查询退款列表
   */
  public List<Refund> listByOrderNumber(String orderNumber, Integer userId) {
    MovieOrder order = getOrderByNumber(orderNumber);
    if (order == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    if (order.getUserId() == null || !order.getUserId().equals(userId)) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
    QueryWrapper<Refund> qw = new QueryWrapper<>();
    qw.eq("order_number", orderNumber).orderByDesc("id");
    return refundMapper.selectList(qw);
  }

  /**
   * 退款详情（校验归属）
   */
  public Refund getDetail(Integer id, Integer userId) {
    Refund refund = refundMapper.selectById(id);
    if (refund == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    if (refund.getUserId() == null || !refund.getUserId().equals(userId)) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
    return refund;
  }

  /**
   * 用户申请退款
   */
  @Transactional
  public Refund apply(String orderNumber, String reason) {
    Integer userId = StpUtil.getLoginIdAsInt();
    MovieOrder order = getOrderByNumber(orderNumber);
    if (order == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    if (order.getUserId() == null || !order.getUserId().equals(userId)) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
    if (!Objects.equals(order.getOrderState(), OrderState.order_succeed.getCode())) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.REFUND_ORDER_NOT_PAID);
    }

    // 防止重复申请：已有 applied/processing 状态的退款则不允许再申请
    QueryWrapper<Refund> existQw = new QueryWrapper<>();
    existQw.eq("order_number", orderNumber)
        .in("apply_status", RefundApplyStatus.applied.getCode(), RefundApplyStatus.processing.getCode());
    if (refundMapper.selectCount(existQw) > 0) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.REFUND_DUPLICATE_APPLY);
    }

    BigDecimal amount = order.getOrderTotal() != null ? order.getOrderTotal() : order.getPayTotal();
    if (amount == null) {
      amount = BigDecimal.ZERO;
    }

    Refund refund = new Refund();
    refund.setOrderNumber(orderNumber);
    refund.setUserId(userId);
    refund.setAmount(amount);
    refund.setReason(reason);
    refund.setApplyStatus(RefundApplyStatus.applied.getCode());
    refund.setRefundState(RefundState.none.getCode());
    refund.setApplyTime(new Date());
    refundMapper.insert(refund);
    return refund;
  }

  /**
   * 我的退款列表（分页）
   */
  public IPage<Refund> myList(RefundListQuery query, Integer userId) {
    Page<Refund> page = new Page<>(query.getPage(), query.getPageSize());
    QueryWrapper<Refund> qw = new QueryWrapper<>();
    qw.eq("user_id", userId).orderByDesc("id");
    if (query.getOrderNumber() != null && !query.getOrderNumber().isBlank()) {
      qw.eq("order_number", query.getOrderNumber());
    }
    if (query.getApplyStatus() != null) {
      qw.eq("apply_status", query.getApplyStatus());
    }
    if (query.getRefundState() != null) {
      qw.eq("refund_state", query.getRefundState());
    }
    return refundMapper.selectPage(page, qw);
  }

  /**
   * 管理端退款列表（分页+筛选）
   */
  public IPage<Refund> adminList(RefundListQuery query) {
    Page<Refund> page = new Page<>(query.getPage(), query.getPageSize());
    QueryWrapper<Refund> qw = new QueryWrapper<>();
    qw.orderByDesc("id");
    if (query.getOrderNumber() != null && !query.getOrderNumber().isBlank()) {
      qw.eq("order_number", query.getOrderNumber());
    }
    if (query.getApplyStatus() != null) {
      qw.eq("apply_status", query.getApplyStatus());
    }
    if (query.getRefundState() != null) {
      qw.eq("refund_state", query.getRefundState());
    }
    return refundMapper.selectPage(page, qw);
  }

  /**
   * 处理退款（同意/拒绝）
   */
  @Transactional
  public Refund process(Integer id, Boolean approved, String rejectReason, Integer processorId) {
    Refund refund = refundMapper.selectById(id);
    if (refund == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    if (!Objects.equals(refund.getApplyStatus(), RefundApplyStatus.applied.getCode())) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.PARAMETER);
    }

    refund.setProcessorId(processorId);
    refund.setProcessTime(new Date());
    if (Boolean.TRUE.equals(approved)) {
      refund.setApplyStatus(RefundApplyStatus.approved.getCode());
      refund.setRefundState(RefundState.refunding.getCode());
      refundMapper.updateById(refund);

      MovieOrder order = getOrderByNumber(refund.getOrderNumber());
      boolean success = paymentService.refund(order != null ? order.getId() : null, refund.getAmount(), "管理员同意退款");

      refund.setRefundState(success ? RefundState.refunded.getCode() : RefundState.refund_failed.getCode());
    } else {
      refund.setApplyStatus(RefundApplyStatus.rejected.getCode());
      if (rejectReason != null && !rejectReason.isBlank()) {
        refund.setReason((refund.getReason() != null ? refund.getReason() + " | " : "") + "拒绝: " + rejectReason);
      }
    }
    refundMapper.updateById(refund);
    return refund;
  }

  /**
   * 用户取消退款申请
   */
  @Transactional
  public void cancelApply(Integer id, Integer userId) {
    Refund refund = refundMapper.selectById(id);
    if (refund == null) {
      throw new BusinessException(ResponseCode.ORDER_NOT_FOUND, MessageKeys.Error.ORDER_NOT_FOUND);
    }
    if (!Objects.equals(refund.getUserId(), userId)) {
      throw new BusinessException(ResponseCode.NOT_PERMISSION, MessageKeys.Error.ORDER_NOT_OWNER);
    }
    if (!Objects.equals(refund.getApplyStatus(), RefundApplyStatus.applied.getCode())) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.PARAMETER);
    }
    refund.setApplyStatus(RefundApplyStatus.rejected.getCode());
    refund.setProcessorId(userId);
    refund.setProcessTime(new Date());
    refund.setReason((refund.getReason() != null ? refund.getReason() + " | " : "") + "用户取消");
    refundMapper.updateById(refund);
  }

  private MovieOrder getOrderByNumber(String orderNumber) {
    QueryWrapper<MovieOrder> qw = new QueryWrapper<>();
    qw.eq("order_number", orderNumber);
    return movieOrderMapper.selectOne(qw);
  }
}
