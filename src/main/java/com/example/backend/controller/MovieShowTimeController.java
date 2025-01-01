package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.*;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.query.MovieShowTimeQuery;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.app.AppMovieShowTimeDetail;
import com.example.backend.service.MovieShowTimeService;
import com.example.backend.service.SeatService;
import com.example.backend.service.SelectSeatService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;


@Data
class SeatPosition {
  @NotNull(message = "${validator.saveSelectSeat.x.required}")
  Integer x;
  @NotNull(message = "${validator.saveSelectSeat.y.required}")
  Integer y;
  @NotNull
  Integer seatId;
}

@Data
class SaveSelectSeatQuery {
  @NotNull(message = "${validator.saveSelectSeat.movieShowTimeId.required}")
  Integer movieShowTimeId;
  @NotNull(message = "${validator.saveSelectSeat.theaterHallId.required}")
  Integer theaterHallId;
  @NotEmpty(message = "${validator.saveSelectSeat.seatPosition.required}")
  List<SeatPosition> seatPosition;
}

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
  private SelectSeatService selectSeatService;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieShowTimeService movieShowTimeService;

  @Autowired
  private SeatService seatService;

  @PostMapping("/api/movie_show_time/list")
  public RestBean<List<MovieShowTimeList>> list(MovieShowTimeListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<MovieShowTime> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieShowTimeList> list = movieShowTimeMapper.movieShowTimeList(query, OrderState.order_succeed.getCode(), page);


    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/app/movie_show_time/detail")
  public RestBean<AppMovieShowTimeDetail> GetAppMovieShowTimeDetail(@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    AppMovieShowTimeDetail result = movieShowTimeMapper.appMovieShowTimeDetail(id);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
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
  @SaCheckLogin
  @PostMapping("/api/movie_show_time/select_seat/save")
  public RestBean<Object> saveSelectSeat(@RequestBody @Validated SaveSelectSeatQuery query) {
    // 判断当前座位是否可选
    QueryWrapper queryWrapper = new QueryWrapper();
    List<Integer> queryX = query.getSeatPosition().stream().map(item -> item.getX()).toList();
    List<Integer> queryY = query.getSeatPosition().stream().map(item -> item.getY()).toList();

    queryWrapper.in("x", queryX);
    queryWrapper.in("y", queryY);
    queryWrapper.eq("theater_hall_id", query.getTheaterHallId());
    queryWrapper.eq("movie_show_time_id", query.getMovieShowTimeId());

    List<SelectSeat> list = selectSeatMapper.selectList(queryWrapper);

    for (SeatPosition item : query.getSeatPosition()) {
      for (SelectSeat children : list) {
        // 判断选择的座位是否已经存在
        if (
          query.getTheaterHallId() == children.getTheaterHallId() &&
          item.getX() == children.getX() &&
          item.getY() == children.getY()
        ) {
          // 如果存在的话，判断是否是同一个人选的
          if (StpUtil.getLoginIdAsInt() != children.getUserId()) {
            // 不是同一个人，报错座位冲突
            return RestBean.error(ResponseCode.ERROR.getCode(), "座位冲突，冲突的座位为：" + item.getX() + "," + item.getY());
          }
        }
      }
    }

    // 保存选座信息
    List<SelectSeat> data = query.getSeatPosition().stream().map(item -> {
      SelectSeat modal = new SelectSeat();
      modal.setMovieShowTimeId(query.getMovieShowTimeId());
      modal.setTheaterHallId(query.getTheaterHallId());
      modal.setX(item.getX());
      modal.setY(item.getY());
      modal.setUserId(StpUtil.getLoginIdAsInt());
      modal.setSelectSeatState(SeatState.selected.getCode());
      modal.setSeatId(item.getSeatId());

      return modal;
    }).toList();
    List<Integer> x = query.getSeatPosition().stream().map(item -> item.getX()).toList();
    List<Integer> y = query.getSeatPosition().stream().map(item -> item.getY()).toList();

    selectSeatMapper.deleteSeat(query.getMovieShowTimeId(), query.getTheaterHallId(), StpUtil.getLoginIdAsInt(), x, y);
    selectSeatService.saveBatch(data);

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @SaCheckLogin
  @GetMapping("/api/movie_show_time/user_select_seat")
  public RestBean<Object> selectSeatList(
    @RequestParam("movieShowTimeId") Integer movieShowTimeId
  ) {
    UserSelectSeat result = movieShowTimeMapper.userSelectSeat(
      StpUtil.getLoginIdAsInt(),
      // 获取用户选择的座位
      movieShowTimeId,
      SeatState.selected.getCode()
    );

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @GetMapping("/api/movie_show_time/select_seat/list")
  public RestBean<Object> selectSeatList(
    @RequestParam("movieShowTimeId") Integer movieShowTimeId,
    @RequestParam("theaterHallId") Integer theaterHallId
  ) {
    Object result = selectSeatService.selectSeatList(theaterHallId, movieShowTimeId);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
}
