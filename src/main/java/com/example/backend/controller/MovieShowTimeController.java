package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.*;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.*;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.query.MovieShowTimeQuery;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.utils.MessageUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
public class MovieShowTimeController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private SeatMapper seatMapper;

  @Autowired
  private SelectSeatMapper selectSeatMapper;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieShowTimeService movieShowTimeService;

  @PostMapping("/api/movie_show_time/list")
  public RestBean<List<MovieShowTimeList>> list(MovieShowTimeListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<MovieShowTime> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieShowTimeList> list = movieShowTimeMapper.movieShowTimeList(page, query);
    List<MovieShowTimeList> result = list.getRecords().stream().map(item -> {
      System.out.println(item);
      QueryWrapper<Seat> seatQueryWrapper = new QueryWrapper<>();
      QueryWrapper<SelectSeat> selectedSeatQueryWrapper = new QueryWrapper<>();
      seatQueryWrapper.eq("theater_hall_id", item.getTheater_hall_id());
      selectedSeatQueryWrapper.eq("movie_show_time_id", item.getId());

      long seatTotal = seatMapper.selectCount(seatQueryWrapper);
      long selectedSeatCount = selectSeatMapper.selectCount(selectedSeatQueryWrapper);

      item.setSeat_total(seatTotal);
      item.setSelected_seat_count(selectedSeatCount);

      return  item;
    }).toList();

    list.setRecords(result);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie_show_time/detail")
  public RestBean<MovieShowTime> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    MovieShowTime result = movieShowTimeMapper.selectById(id);

    return RestBean.success(result, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieShowTime.remove")
  @Transactional
  @DeleteMapping("/api/admin/movie_show_time/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("movie_show_time_id", id);

    movieShowTimeMapper.deleteById(id);
    selectSeatMapper.delete(wrapper);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieShowTime.save")
  @PostMapping("/api/admin/movie_show_time/save")
  public RestBean<Object> save(@RequestBody @Validated MovieShowTimeQuery query) throws ParseException {
    String format = "yyyy-MM-dd HH:mm:ss";
    List<MovieShowTime> list = movieShowTimeService.getSortedMovieShowTimes(
      query, format
    );
    Boolean result = movieShowTimeService.check(list, format, query);

    return result ? RestBean.success(null, MessageUtils.getMessage("success.save")) : RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.timeConflict"));
  }
  @GetMapping("/api/movie_show_time/select_seat/list")
  public RestBean<Object> selectSeatList(@RequestParam Integer id) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("id", id);
    MovieShowTime movieShowTime = movieShowTimeMapper.selectOne(wrapper);
    List list = selectSeatMapper.selectSeatList(movieShowTime.getTheaterHallId());

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
}
