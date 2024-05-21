package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.TheaterHall;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.mapper.TheaterHallMapper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
public class TheaterHallController {
  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private SeatMapper seatMapper;

  @Autowired
  private SelectSeatMapper selectSeatMapper;

  @PostMapping("/api/theater/hall/list")
  public RestBean<List<TheaterHall>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<TheaterHall> page = new Page<>(query.getPage() - 1, query.getPageSize());

    IPage list = theaterHallMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/theater/hall/seat")
  public RestBean<Object> seatList(@RequestParam Integer theaterHallId) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("theater_hall_id", theaterHallId);

    List list = seatMapper.selectList(wrapper);

    return RestBean.success(list, "获取成功");
  }
  @PostMapping("/api/theater/hall/save")
  public RestBean<String> save() {
    return RestBean.success(null, "success");
  }
  @DeleteMapping("/api/theater/hall/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    return RestBean.success(null, "success");
  }
}
