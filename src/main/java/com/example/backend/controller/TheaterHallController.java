package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.*;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.mapper.TheaterHallMapper;
import com.example.backend.query.SaveSeatQuery;
import com.example.backend.service.SeatService;
import com.example.backend.utils.MessageUtils;
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


@Data
class TheaterHallQuery {
  private Integer page;
  private Integer pageSize;
  private Integer cinemaId;
  private String name;

  public TheaterHallQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

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


  @PostMapping("/api/theater/hall/list")
  public RestBean<List<TheaterHall>> list(@RequestBody TheaterHallQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());
//    wrapper.orderByAsc("update_time");
    Page<TheaterHall> page = new Page<>(query.getPage(), query.getPageSize());

    IPage list = theaterHallMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/theater/hall/detail")
  public RestBean<TheaterHall> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    TheaterHall result = theaterHallMapper.selectById(id);

    return RestBean.success(result, MessageUtils.getMessage("success.remove"));
  }
  @GetMapping("/api/theater/hall/seat/detail")
  public RestBean<Object> seatList(@RequestParam Integer theaterHallId) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("theater_hall_id", theaterHallId);

    Object list = seatService.seatList(theaterHallId);

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }

  @SaCheckLogin
  @CheckPermission(code = "theaterHall.saveSeatConfig")
  @Transactional
  @PostMapping("/api/theater/hall/seat/save")
  public RestBean<String> saveSeat(@RequestBody SaveSeatQuery query) {

    seatService.saveSeat(query);

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @Transactional
  public  void buildSeat (Integer id, TheaterHallSaveQuery query) {
    seatMapper.deleteSeat(id);

    // 创建座位列表
    List<Seat> seats = new ArrayList<>();
    for (int i = 0; i <= query.getRowCount(); i++) {
      for (int j = 0; j <= query.getColumnCount(); j++) {
        Seat seat = new Seat();
        seat.setXAxis(i);
        seat.setYAxis(j);
        seat.setTheaterHallId(id);
        seats.add(seat);
      }
    }

    // 批量插入座位
    seatService.saveBatch(seats);
  }

  @SaCheckLogin
  @CheckPermission(code = "theaterHall.save")
  @Transactional
  @PostMapping("/api/admin/theater/hall/save")
  public RestBean<String> save(@RequestBody @Validated() TheaterHallSaveQuery query) {
    String message = MessageUtils.getMessage("error.repeat", MessageUtils.getMessage("repeat.theaterHallName"));
    TheaterHall modal = new TheaterHall();

    modal.setName(query.getName());
    modal.setCinemaId(query.getCinemaId());
    modal.setCinemaSpecId(query.getCinemaSpecId());

    if (query.getId() == null) {
      modal.setRowCount(query.getRowCount());
      modal.setColumnCount(query.getColumnCount());
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
        return RestBean.error(ResponseCode.REPEAT.getCode(), message);
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
        return RestBean.error(
          ResponseCode.REPEAT.getCode(),
          MessageUtils.getMessage("error.repeat", message)
        );
      }
    }
    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @SaCheckLogin
  @CheckPermission(code = "theaterHall.remove")
  @DeleteMapping("/api/admin/theater/hall/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
}
