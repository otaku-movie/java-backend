package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.mapper.TheaterHallMapper;
import com.example.backend.query.SaveSeatQuery;
import com.example.backend.response.TheaterHallList;
import com.example.backend.service.SeatService;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@RestController
public class TheaterHallController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private SeatMapper seatMapper;
  @Autowired
  private SeatService seatService;

  @Autowired
  private SelectSeatMapper selectSeatMapper;


  @PostMapping(ApiPaths.Common.TheaterHall.LIST)
  public RestBean<List<TheaterHallList>> list(@RequestBody TheaterHallQuery query)  {
    Page<TheaterHall> page = new Page<>(query.getPage(), query.getPageSize());

    IPage list = theaterHallMapper.theaterHallList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.TheaterHall.DETAIL)
  public RestBean<TheaterHall> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    TheaterHall result = theaterHallMapper.selectById(id);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @GetMapping(ApiPaths.Common.TheaterHall.SEAT_DETAIL)
  public RestBean<Object> seatList(@RequestParam Integer theaterHallId) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("theater_hall_id", theaterHallId);

    Object list = seatService.seatList(theaterHallId);

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "theaterHall.saveSeatConfig")
  @Transactional
  @PostMapping(ApiPaths.Common.TheaterHall.SEAT_SAVE)
  public RestBean<String> saveSeat(@RequestBody SaveSeatQuery query) {

    seatService.saveSeat(query);

    return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
  }
  @Transactional
  public  void buildSeat (Integer id, TheaterHallSaveQuery query) {
    seatMapper.deleteSeat(id);

    // 创建座位列表
    List<Seat> seats = new ArrayList<>();
    for (int i = 0; i < query.getRowCount(); i++) {
      for (int j = 0; j < query.getColumnCount(); j++) {
        Seat seat = new Seat();
        String rowName = Utils.getRowName(i);
        seat.setXAxis(i);
        seat.setYAxis(j);
        seat.setTheaterHallId(id);
        seat.setRowName(rowName);
        seat.setSeatName(String.format("%s-%d", rowName, j + 1));
        seats.add(seat);
      }
    }

    // 批量插入座位
    seatService.saveBatch(seats);
  }

  @SaCheckLogin
  @CheckPermission(code = "theaterHall.save")
  @Transactional
  @PostMapping(ApiPaths.Admin.TheaterHall.SAVE)
  public RestBean<String> save(@RequestBody @Validated() TheaterHallSaveQuery query) {
    String repeatMessage = MessageUtils.getMessage(MessageKeys.Admin.REPEAT_ERROR, MessageUtils.getMessage(MessageKeys.Admin.Repeat.THEATER_HALL_NAME));
    TheaterHall modal = new TheaterHall();

    modal.setName(query.getName());
    modal.setCinemaId(query.getCinemaId());
    modal.setCinemaSpecId(query.getCinemaSpecId());

    if (query.getId() == null) {
      modal.setRowCount(query.getRowCount());
      modal.setColumnCount(query.getColumnCount());
      modal.setSeatNamingRules("{alphabet}-{columnNumber}");
    }

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("cinema_id", modal.getCinemaId());
      wrapper.eq("name", query.getName());
      List<MovieTicketType> list = theaterHallMapper.selectList(wrapper);

      if (list.size() == 0) {
        theaterHallMapper.insert(modal);
        buildSeat(modal.getId(), query);
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 判断编辑是否重复，去掉当前的，如果path已存在就算重复
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("cinema_id", query.getCinemaId());
      queryWrapper.eq("name", query.getName());
      queryWrapper.ne("id", query.getId());

      List<Menu> data = theaterHallMapper.selectList(queryWrapper);
      if (data.size() == 0) {
        modal.setId(query.getId());
        // 更新
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());
        theaterHallMapper.update(modal, updateQueryWrapper);
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    }
    return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "theaterHall.remove")
  @DeleteMapping(ApiPaths.Admin.TheaterHall.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    QueryWrapper queryWrapper = new QueryWrapper<>();

    queryWrapper.eq("id", id);
    theaterHallMapper.delete(queryWrapper);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
}
