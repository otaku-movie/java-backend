package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.enumerate.SeatState;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.UserSelectSeat;
import com.example.backend.response.chart.StatisticsOfDailyMovieScreenings;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface MovieShowTimeMapper extends BaseMapper<MovieShowTime> {
  List<UserSelectSeat> userSelectSeat(Integer userId, Integer movieShowTimeId, Integer seatState);
  IPage<MovieShowTimeList> movieShowTimeList(MovieShowTimeListQuery query, Integer orderState, Page<MovieShowTime> page);
  List<MovieShowTimeList> movieShowTimeList(MovieShowTimeListQuery query, Integer orderState);
  List<StatisticsOfDailyMovieScreenings>  StatisticsOfDailyMovieScreenings();
}