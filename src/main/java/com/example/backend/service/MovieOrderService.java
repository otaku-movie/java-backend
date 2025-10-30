package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.query.order.MyTicketsQuery;
import com.example.backend.query.order.UpdateOrderStateQuery;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.UserSelectSeatList;
import com.example.backend.response.Spec;
import com.example.backend.response.order.MovieOrderSeat;
import com.example.backend.response.order.MyTicketsResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
class SeatGroupQuery {
  Integer x;
  Integer y;
  Integer seatId;
  Integer movieTicketTypeId;
  Integer theaterHallId;
  BigDecimal movieTicketTypePrice;
  BigDecimal areaPrice;
  BigDecimal plusPrice;
}

@Service
public class MovieOrderService extends ServiceImpl<MovieOrderMapper, MovieOrder> {
  @Autowired
  MovieShowTimeMapper movieShowTimeMapper;
  @Autowired
  SelectSeatService selectSeatService;

  @Autowired
  SelectSeatMapper selectSeatMapper;

  @Autowired
  MovieTicketTypeMapper movieTicketTypeMapper;

  @Autowired
  MovieOrderMapper movieOrderMapper;

  @Autowired
  PaymentMethodMapper paymentMethodMapper;

  @Autowired
  PaymentService paymentMethodService;

  @Autowired
  CinemaMapper cinemaMapper;

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public MovieOrder createOrder(MovieOrderSaveQuery query) throws Exception {
    Integer movieShowTimeId = query.getMovieShowTimeId();
    Integer userId = StpUtil.getLoginIdAsInt();
    UserSelectSeat result = movieShowTimeMapper.userSelectSeatWithoutSpec(
      userId,
      movieShowTimeId,
      SeatState.selected.getCode()
    );

    if (result == null) {
      throw new Exception("234");
    }

    // 获取影院规格信息
    List<Spec> cinemaSpecs = cinemaMapper.getCinemaSpec(result.getCinemaId());
    
    List<SeatGroupQuery> data = query.getSeat().stream().map(item -> {
      SeatGroupQuery modal = new SeatGroupQuery();
      modal.setX(item.getX());
      modal.setY(item.getY());

      UserSelectSeatList userSelectSeat = result.getSeat().stream().filter(children -> {
        return children.getX().equals(item.getX()) && children.getY().equals(item.getY());
        })
        .findFirst()
        .orElse(null);

      MovieTicketType movieTicketType = movieTicketTypeMapper.selectById(item.getMovieTicketTypeId());
      modal.setTheaterHallId(result.getTheaterHallId());
      modal.setAreaPrice(userSelectSeat.getAreaPrice());
      modal.setMovieTicketTypeId(movieTicketType.getId());
      modal.setMovieTicketTypePrice(movieTicketType.getPrice());
      
      // 从影院规格中获取plusPrice (假设使用第一个规格，或者根据具体业务逻辑选择)
      BigDecimal plusPrice = cinemaSpecs.isEmpty() ? BigDecimal.ZERO : 
        new BigDecimal(cinemaSpecs.get(0).getPlusPrice() != null ? cinemaSpecs.get(0).getPlusPrice() : "0");
      modal.setPlusPrice(plusPrice);
      
      modal.setSeatId(item.getSeatId());

      return  modal;
    }).toList();
    // 计算总价
    BigDecimal total = data.stream().reduce(BigDecimal.ZERO, (sum, current) -> {
      if (current.getAreaPrice() != null) {
        sum = sum.add(current.getAreaPrice());
      }
      if (current.getMovieTicketTypePrice() != null) {
        sum = sum.add(current.getMovieTicketTypePrice());
      }
      if (current.getPlusPrice() != null) {
        sum = sum.add(current.getPlusPrice());
      }
      return sum;
    }, BigDecimal::add);
    // 创建订单
    MovieOrder movieOrder = new MovieOrder();
    movieOrder.setMovieShowTimeId(query.getMovieShowTimeId());
    movieOrder.setOrderState(OrderState.order_created.getCode());
    movieOrder.setOrderTotal(total);
    movieOrder.setPayState(PayState.waiting_for_payment.getCode());

    movieOrderMapper.insert(movieOrder);

    List<Integer> x = data.stream().map(item -> item.getX()).toList();
    List<Integer> y = data.stream().map(item -> item.getY()).toList();

    // 检查座位是否被其他用户选择（并发检查）
    if (!isSeatsAvailable(movieShowTimeId, data.get(0).getTheaterHallId(), x, y, userId)) {
      throw new Exception("座位已被其他用户选择，请重新选择");
    }

    // 移除旧的选座信息
    selectSeatMapper.deleteSeat(query.getMovieShowTimeId(), data.get(0).getTheaterHallId(), StpUtil.getLoginIdAsInt(), x, y);

    // 更新用户选座信息
    List<SelectSeat> newSelectSeat = data.stream().map(item -> {
      SelectSeat userSelectSeat = new SelectSeat();

      userSelectSeat.setUserId(StpUtil.getLoginIdAsInt());
      userSelectSeat.setMovieShowTimeId(query.getMovieShowTimeId());
      userSelectSeat.setTheaterHallId(item.getTheaterHallId());
      userSelectSeat.setX(item.getX());
      userSelectSeat.setY(item.getY());
      userSelectSeat.setSeatId(item.getSeatId());
      userSelectSeat.setMovieTicketTypeId(item.getMovieTicketTypeId());
      userSelectSeat.setMovieOrderId(movieOrder.getId());
      userSelectSeat.setSelectSeatState(SeatState.locked.getCode());
      userSelectSeat.setMovieTicketTypeId(item.getMovieTicketTypeId());

      return  userSelectSeat;
    }).toList();

    selectSeatService.saveBatch(newSelectSeat);

    return movieOrder;
  }

  /**
   * 检查座位是否可用（并发控制）
   */
  private boolean isSeatsAvailable(Integer movieShowTimeId, Integer theaterHallId, List<Integer> x, List<Integer> y, Integer userId) {
    // 检查是否有其他用户已经选择了这些座位（排除当前用户）
    Integer count = selectSeatMapper.countSeatsByCoordinates(movieShowTimeId, theaterHallId, x, y, userId);
    return count == 0;
  }

  @Transactional
  public void pay(Integer orderId, Integer payId) {
    // 增加支付失败概率（30%失败率）
    Random random = new Random();
    if (random.nextDouble() < 0.3) {
      // 支付失败，更新订单状态
      MovieOrder movieOrder = new MovieOrder();
      movieOrder.setId(orderId);
      movieOrder.setOrderState(OrderState.order_failed.getCode()); // 设置订单状态为失败
      movieOrder.setPayState(PayState.payment_failed.getCode()); // 设置支付状态为失败
      movieOrderMapper.updateById(movieOrder);
      
      // 恢复座位状态
      SelectSeat selectSeat = new SelectSeat();
      UpdateWrapper updateWrapper = new UpdateWrapper();
      selectSeat.setSelectSeatState(SeatState.available.getCode());
      updateWrapper.eq("movie_order_id", orderId);
      selectSeatMapper.update(selectSeat, updateWrapper);
      
      System.out.println("支付失败，订单ID: " + orderId);
      return;
    }

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    MovieOrder movieOrder = new MovieOrder();
    movieOrder.setId(orderId);
    SelectSeat selectSeat = new SelectSeat();

    // 更新用户选座状态
    UpdateWrapper updateWrapper = new UpdateWrapper();

    selectSeat.setSelectSeatState(SeatState.sold.getCode());
    updateWrapper.eq("movie_order_id", orderId);
    selectSeatMapper.update(selectSeat, updateWrapper);

    // 初始更新为支付中
//    movieOrder.setOrderState(OrderState.order_paying.getCode()); // 设置订单状态为支付中
    movieOrder.setPayState(PayState.paying.getCode());           // 设置支付状态为支付中
    movieOrder.setPayMethodId(payId);                              // 设置支付方式
    movieOrder.setPayTime(new Date());                           // 设置支付时间（支付流程开始时间）
    movieOrderMapper.updateById(movieOrder);                    // 更新订单状态到数据库

    // 调用支付
    paymentMethodService.pay();

    // 30 秒后模拟支付完成，增加失败概率
    scheduler.schedule(() -> {
      try {
        // 增加支付失败概率（20%失败率）
        Random random2 = new Random();
        if (random2.nextDouble() < 0.2) {
          // 支付失败，更新订单状态
          movieOrder.setOrderState(OrderState.order_failed.getCode()); // 设置订单状态为失败
          movieOrder.setPayState(PayState.payment_failed.getCode()); // 设置支付状态为失败
          movieOrderMapper.updateById(movieOrder);
          
          // 恢复座位状态
          selectSeat.setSelectSeatState(SeatState.available.getCode());
          selectSeatMapper.update(selectSeat, updateWrapper);
          
          System.out.println("支付失败，订单ID: " + orderId);
          return;
        }
        
        // 更新为支付成功
//        movieOrder.setPayTotal();
        movieOrder.setOrderState(OrderState.order_succeed.getCode()); // 设置订单状态为成功
        movieOrder.setPayState(PayState.payment_successful.getCode()); // 设置支付状态为成功
        movieOrder.setPayTime(new Date());                             // 设置支付完成时间
        movieOrderMapper.updateById(movieOrder);                      // 更新订单状态到数据库
        
        System.out.println("支付成功，订单ID: " + orderId);
      } catch (Exception e) {
        e.printStackTrace(); // 打印异常日志
      } finally {
        // 确保定时器正常关闭
        scheduler.shutdown();
      }
    }, 30, TimeUnit.SECONDS);
  }
  List<SelectSeat> findSelectSeat (Integer movieOrderId) {
    QueryWrapper querySelectSeatWrapper = new QueryWrapper();
    querySelectSeatWrapper.eq("movie_order_id", movieOrderId);

    List<SelectSeat> userSeatList = selectSeatMapper.selectList(querySelectSeatWrapper);

    return userSeatList;
  }
  void removeSeat (List<SelectSeat> userSeatList) {
    SelectSeat seatItem = userSeatList.get(0);
    List<Integer> x = userSeatList.stream().map(item -> item.getX()).toList();
    List<Integer> y = userSeatList.stream().map(item -> item.getY()).toList();

    selectSeatMapper.deleteSeat(seatItem.getMovieShowTimeId(), seatItem.getTheaterHallId(), seatItem.getUserId(), x, y);
  }
  @Transactional
  public  void updateOrderState(UpdateOrderStateQuery query) {
    MovieOrder movieOrder = movieOrderMapper.selectById(query.getId());

    // 订单已创建
    if (movieOrder.getOrderState() == OrderState.order_created.getCode()) {
      // 订单已完成 (修改订单状态为已完成，座位状态为已售出，支付状态为已完成)
      if (query.getOrderState() == OrderState.order_succeed.getCode()) {
        // 查询之前的选座
        List<SelectSeat> userSeatList = findSelectSeat(query.getId());

        // 删除旧的选座
        this.removeSeat(userSeatList);

        // 创建新的选座
        List<SelectSeat> newSeatData = userSeatList.stream().map(item -> {
          item.setSelectSeatState(SeatState.sold.getCode());

          return item;
        }).toList();

        selectSeatService.saveBatch(newSeatData);

        // 更新订单状态
        movieOrder.setId(query.getId());
        movieOrder.setOrderState(OrderState.order_succeed.getCode());
        movieOrder.setPayState(PayState.payment_successful.getCode());
        movieOrder.setPayTime(new Date());
        movieOrder.setUpdateTime(new Date());

        movieOrderMapper.updateById(movieOrder);
      }
      // 订单失败
      if (query.getOrderState() == OrderState.order_failed.getCode()) {

      }
      // 取消订单 删除选座，设置订单状态为取消订单
      if (query.getOrderState() == OrderState.canceled_order.getCode()) {
        movieOrder.setId(query.getId());
        movieOrder.setOrderState(OrderState.canceled_order.getCode());

        movieOrderMapper.updateById(movieOrder);

        // 查询之前的选座
        List<SelectSeat> userSeatList = findSelectSeat(query.getId());

        // 删除旧的选座
        QueryWrapper deleteQueryWrapper = new QueryWrapper();
        deleteQueryWrapper.eq("movie_order_id", query.getId());

        selectSeatMapper.delete(deleteQueryWrapper);
      }
      // 订单超时 删除选座，设置订单为超时订单
      if (query.getOrderState() == OrderState.order_timeout.getCode()) {
        movieOrder.setId(query.getId());
        movieOrder.setOrderState(OrderState.order_timeout.getCode());

        // 查询之前的选座
        List<SelectSeat> userSeatList = findSelectSeat(query.getId());

        // 删除旧的选座
        this.removeSeat(userSeatList);
      }
    }

  }

  @Transactional
  public void updateCancelOrTimeoutOrder(Integer orderId, String state) {
    MovieOrder movieOrder = movieOrderMapper.selectById(orderId);
    
    // 只有已创建的订单才能取消
    if (movieOrder.getOrderState() != OrderState.order_created.getCode()) {
      throw new RuntimeException("只有已创建的订单才能取消");
    }

    if (state == "cancel") {
      // 更新订单状态为取消
      movieOrder.setId(orderId);
      movieOrder.setOrderState(OrderState.canceled_order.getCode());
      movieOrderMapper.updateById(movieOrder);
    }
    if (state == "timeout") {
      // 更新订单状态为取消
      movieOrder.setId(orderId);
      movieOrder.setOrderState(OrderState.order_timeout.getCode());
      movieOrderMapper.updateById(movieOrder);
    }

    // 删除选座信息
    QueryWrapper<SelectSeat> deleteQueryWrapper = new QueryWrapper<>();
    deleteQueryWrapper.eq("movie_order_id", orderId);
    selectSeatMapper.delete(deleteQueryWrapper);
  }

  public List<MyTicketsResponse> getMyTickets(Integer userId) {
    // 第一步：获取用户的有效订单ID列表
    List<Integer> orderIds = movieOrderMapper.getUserValidOrderIds(userId);
    
    if (orderIds == null || orderIds.isEmpty()) {
      return Collections.emptyList();
    }
    
    // 第二步：批量获取订单基本信息
    List<MyTicketsResponse> tickets = movieOrderMapper.getMyTicketsByIds(orderIds);
    
    // 第三步：批量获取座位信息
    List<MovieOrderSeat> seatList = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds);
    
    // 第四步：按订单ID分组座位信息
    Map<Integer, List<MovieOrderSeat>> seatMap = seatList.stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId));
    
    // 第五步：设置座位信息到对应的订单
    tickets.forEach(ticket -> {
      List<MovieOrderSeat> seats = seatMap.getOrDefault(ticket.getId(), Collections.emptyList());
      ticket.setSeat(seats);
    });
    
    return tickets;
  }

  public IPage<MyTicketsResponse> getMyTicketsPage(MyTicketsQuery query) {
    // 设置用户ID
    query.setUserId(StpUtil.getLoginIdAsInt());
    
    // 第一步：获取用户的有效订单ID列表（带分页）
    Page<Integer> orderIdPage = new Page<>(query.getPage(), query.getPageSize());
    IPage<Integer> orderIdResult = movieOrderMapper.getUserValidOrderIdsPage(query, orderIdPage);
    
    if (orderIdResult.getRecords() == null || orderIdResult.getRecords().isEmpty()) {
      // 返回空的分页结果
      Page<MyTicketsResponse> emptyPage = new Page<>(query.getPage(), query.getPageSize());
      emptyPage.setTotal(0);
      return emptyPage;
    }
    
    List<Integer> orderIds = orderIdResult.getRecords();
    
    // 第二步：批量获取订单基本信息
    List<MyTicketsResponse> tickets = movieOrderMapper.getMyTicketsByIds(orderIds);
    
    // 第三步：批量获取座位信息
    List<MovieOrderSeat> seatList = movieOrderMapper.getMovieOrderSeatListByOrderIds(orderIds);
    
    // 第四步：按订单ID分组座位信息
    Map<Integer, List<MovieOrderSeat>> seatMap = seatList.stream()
      .collect(Collectors.groupingBy(MovieOrderSeat::getMovieOrderId));
    
    // 第五步：设置座位信息到对应的订单
    tickets.forEach(ticket -> {
      List<MovieOrderSeat> seats = seatMap.getOrDefault(ticket.getId(), Collections.emptyList());
      ticket.setSeat(seats);
    });
    
    // 第六步：构造分页结果
    Page<MyTicketsResponse> result = new Page<>(query.getPage(), query.getPageSize());
    result.setRecords(tickets);
    result.setTotal(orderIdResult.getTotal());
    
    return result;
  }
}
