package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.MovieOrder;
import com.example.backend.query.order.MovieOrderListQuery;
import com.example.backend.response.order.OrderListResponse;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MovieOrderMapper extends BaseMapper<MovieOrder> {
  IPage<OrderListResponse> orderList(MovieOrderListQuery query, Page<OrderListResponse> page);
}