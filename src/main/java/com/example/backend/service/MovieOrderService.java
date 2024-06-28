package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.SelectSeat;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.PayState;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.query.order.MovieOrderSaveQuery;
import com.example.backend.response.UserSelectSeat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Data
class SeatGroupQuery {
  Integer x;
  Integer y;
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


  @Transactional
  public void createOrder(MovieOrderSaveQuery query) {
    Integer movieShowTimeId = query.getMovieShowTimeId();
    List<UserSelectSeat> result = movieShowTimeMapper.userSelectSeat(StpUtil.getLoginIdAsInt(), movieShowTimeId, SeatState.locked.getCode());

    List<SeatGroupQuery> data = query.getSeat().stream().map(item -> {
      SeatGroupQuery modal = new SeatGroupQuery();
      modal.setX(item.getX());
      modal.setY(item.getY());

      UserSelectSeat userSelectSeat = result.stream().filter(children -> children.getX().equals(item.getX()) && children.getY().equals(item.getY()))
        .findFirst()
        .orElse(null);

      MovieTicketType movieTicketType = movieTicketTypeMapper.selectById(item.getMovieTicketTypeId());
      modal.setTheaterHallId(userSelectSeat.getTheaterHallId());
      modal.setAreaPrice(userSelectSeat.getAreaPrice());
      modal.setMovieTicketTypeId(movieTicketType.getId());
      modal.setMovieTicketTypePrice(movieTicketType.getPrice());
      modal.setPlusPrice(userSelectSeat.getPlusPrice());

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
      userSelectSeat.setMovieTicketTypeId(item.getMovieTicketTypeId());
      userSelectSeat.setMovieOrderId(movieOrder.getId());
      userSelectSeat.setSelectSeatState(SeatState.locked.getCode());
      userSelectSeat.setMovieTicketTypeId(item.getMovieTicketTypeId());

      return  userSelectSeat;
    }).toList();

    selectSeatService.saveBatch(newSelectSeat);
  }

}
