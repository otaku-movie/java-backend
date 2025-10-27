package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.MovieOrder;
import com.example.backend.query.order.MovieOrderListQuery;
import com.example.backend.query.order.MyTicketsQuery;
import com.example.backend.response.chart.DailyOrderStatistics;
import com.example.backend.response.chart.DailyTransactionAmount;
import com.example.backend.response.order.MovieOrderSeat;
import com.example.backend.response.order.MyTicketsResponse;
import com.example.backend.response.order.OrderListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface MovieOrderMapper extends BaseMapper<MovieOrder> {
  // 查询多个订单的座位信息
  List<MovieOrderSeat> getMovieOrderSeatListByOrderIds(List<Integer> orderIds);
  // 查询单个订单的座位信息
  List<MovieOrderSeat> getMovieOrderSeatList(Integer orderId);
  IPage<OrderListResponse> orderList(MovieOrderListQuery query, Page<OrderListResponse> page);
  OrderListResponse orderDetail(Integer orderId);
  IPage<OrderListResponse>  userOrderList(Integer userId,  Page<OrderListResponse> page);
  List<DailyOrderStatistics> DailyOrderStatistics();
  List<DailyTransactionAmount> DailyTransactionAmount(Integer orderState, Integer payState);
  List<MyTicketsResponse> getMyTickets(Integer userId);
  IPage<MyTicketsResponse> getMyTicketsPage(MyTicketsQuery query, Page<MyTicketsResponse> page);
  
  // 拆分后的查询方法
  List<Integer> getUserValidOrderIds(Integer userId);
  IPage<Integer> getUserValidOrderIdsPage(MyTicketsQuery query, Page<Integer> page);
  List<MyTicketsResponse> getMyTicketsByIds(List<Integer> orderIds);
}