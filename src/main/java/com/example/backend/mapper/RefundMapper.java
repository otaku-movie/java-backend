package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefundMapper extends BaseMapper<Refund> {

  /**
   * 获取订单最新退款的退款状态（按订单号）
   */
  Integer getLatestRefundState(@Param("orderNumber") String orderNumber);

  /**
   * 获取订单最新退款的申请状态（按订单号）
   */
  Integer getLatestApplyStatus(@Param("orderNumber") String orderNumber);
}
