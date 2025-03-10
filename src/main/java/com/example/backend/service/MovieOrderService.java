package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.query.order.UpdateOrderStateQuery;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.UserSelectSeatList;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  @Transactional
  public MovieOrder createOrder(MovieOrderSaveQuery query) throws Exception {
    Integer movieShowTimeId = query.getMovieShowTimeId();
    Integer userId = StpUtil.getLoginIdAsInt();
    UserSelectSeat result = movieShowTimeMapper.userSelectSeat(
      userId,
      movieShowTimeId,
      SeatState.selected.getCode()
    );

    if (result == null) {
      throw new Exception("234");
    }

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
      modal.setPlusPrice(userSelectSeat.getPlusPrice());
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

  @Transactional
  public void pay(Integer orderId, Integer payId) {
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

    // 30 秒后模拟支付完成
    scheduler.schedule(() -> {
      try {
        // 更新为支付成功
//        movieOrder.setPayTotal();
        movieOrder.setOrderState(OrderState.order_succeed.getCode()); // 设置订单状态为成功
        movieOrder.setPayState(PayState.payment_successful.getCode()); // 设置支付状态为成功
        movieOrder.setPayTime(new Date());                             // 设置支付完成时间
        movieOrderMapper.updateById(movieOrder);                      // 更新订单状态到数据库
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
}
