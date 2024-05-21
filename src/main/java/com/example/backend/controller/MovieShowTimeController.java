package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.*;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.mapper.TheaterHallMapper;
import com.example.backend.response.MovieShowTimeList;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;


@RestController
public class MovieShowTimeController {
  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private SeatMapper seatMapper;

  @Autowired
  private SelectSeatMapper selectSeatMapper;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @PostMapping("/api/movie_show_time/list")
  public RestBean<List<MovieShowTimeList>> list()  {
//    QueryWrapper wrapper = new QueryWrapper<>();
//    Page<TheaterHall> page = new Page<>(query.getPage() - 1, query.getPageSize());

    List<MovieShowTimeList> list = movieShowTimeMapper.movieShowTimeList();
    List<MovieShowTimeList> result = list.stream().map(item -> {
      System.out.println(item);
      QueryWrapper<Seat> seatQueryWrapper = new QueryWrapper<>();
      QueryWrapper<SelectSeat> selectedSeatQueryWrapper = new QueryWrapper<>();
      seatQueryWrapper.eq("theater_hall_id", item.getTheaterHallId());
      selectedSeatQueryWrapper.eq("movie_show_time_id", item.getId());

      long seatTotal = seatMapper.selectCount(seatQueryWrapper);
      long selectedSeatCount = selectSeatMapper.selectCount(selectedSeatQueryWrapper);

      item.setSeatTotal(seatTotal);
      item.setSelectedSeatCount(selectedSeatCount);

      return  item;
    }).toList();

    return RestBean.success(result, "获取成功");
  }
  @GetMapping("/api/movie_show_time/select_seat/list")
  public RestBean<Object> selectSeatList(@RequestParam Integer id) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("id", id);
    MovieShowTime movieShowTime = movieShowTimeMapper.selectOne(wrapper);
    List list = selectSeatMapper.selectSeatList(movieShowTime.getTheaterHallId());

    return RestBean.success(list, "获取成功");
  }
}
