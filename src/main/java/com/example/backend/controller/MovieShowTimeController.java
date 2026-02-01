package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.SeatState;
import com.example.backend.mapper.*;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.query.MovieShowTimeQuery;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.showTime.MovieShowTimeDetail;

import java.math.BigDecimal;
import java.util.List;
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
import java.util.*;


@Data
class SeatPosition implements com.example.backend.service.SelectSeatService.SeatPositionInterface {
  @NotNull(message = "${validator.saveSelectSeat.x.required}")
  Integer x;
  @NotNull(message = "${validator.saveSelectSeat.y.required}")
  Integer y;
  @NotNull
  Integer seatId;
  @NotNull
  String seatName;
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

@Data
class CancelSelectSeatQuery {
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
  CinemaMapper cinemaMapper;

  @Autowired
  private SelectSeatMapper selectSeatMapper;

  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieShowTimeService movieShowTimeService;

  @PostMapping(ApiPaths.Common.ShowTime.LIST)
  public RestBean<List<MovieShowTimeList>> list(MovieShowTimeListQuery query)  {
    Page<MovieShowTime> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieShowTimeList> list = movieShowTimeMapper.movieShowTimeList(query, OrderState.order_succeed.getCode(), page);
    Map<Integer, Language> languageMap = new HashMap<>();
    Set<Integer> languageSet = new HashSet<>();

    List<MovieShowTimeList> result = list.getRecords().stream().map(item -> {
      // 获取字幕的语言存到set里面，从而减少查询次数
      if (item.getSubtitleId() != null) {
        item.getSubtitleId().forEach(children -> languageSet.add(children));

      }
      item.setMovieShowTimeTags(
        movieShowTimeMapper.getMovieShowTimeTags(item.getMovieShowTimeTagsId())
      );


      return item;
    }).toList();

    // 根据set里面的数据去数据库查询然后存到map里面
    movieShowTimeMapper.getMovieShowTimeSubtitle(languageSet.stream().toList()).stream().forEach(item -> {
      languageMap.put(item.getId(), item);
    });

    // 从map里面获取数据，根据数组的值从map获取值进行添加
    result.stream().forEach(item -> {
      if (item.getSubtitleId() != null && item.getSubtitleId().size() != 0) {
        List<Language> languageList = new ArrayList<>();
        item.getSubtitleId().stream().forEach(id -> {
          languageList.add(languageMap.get(id));
        });
        item.setSubtitle(
          languageList
        );
      } else {
        item.setSubtitle(new ArrayList<>());
      }
    });

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.ShowTime.DETAIL)
  public RestBean<MovieShowTimeDetail> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    MovieShowTimeDetail result = movieShowTimeMapper.movieShowTimeDetail(id);
    result.setMovieShowTimeTags(
      movieShowTimeMapper.getMovieShowTimeTags(result.getMovieShowTimeTagsId())
    );
    result.setSubtitle(
      movieShowTimeMapper.getMovieShowTimeSubtitle(result.getSubtitleId())
    );

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieShowTime.remove")
  @Transactional
  @DeleteMapping(ApiPaths.Admin.ShowTime.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    QueryWrapper<SelectSeat> wrapper = new QueryWrapper<>();
    wrapper.eq("movie_show_time_id", id);

    movieShowTimeMapper.deleteById(id);
    selectSeatMapper.delete(wrapper);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieShowTime.save")
  @PostMapping(ApiPaths.Admin.ShowTime.SAVE)
  public RestBean<Object> save(@RequestBody @Validated MovieShowTimeQuery query) throws ParseException {
    String format = "yyyy-MM-dd HH:mm:ss";
    List<MovieShowTime> list = movieShowTimeService.getSortedMovieShowTimes(
      query, format
    );
    Boolean result = movieShowTimeService.check(list, format, query);

    return result ? RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS)) : RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.TIME_CONFLICT));
  }
}
