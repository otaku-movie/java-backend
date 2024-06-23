package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.UserSelectSeat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface MovieShowTimeMapper extends BaseMapper<MovieShowTime> {
  List<Object> userSelectSeat(Integer userId, Integer movieShowTimeId);
  IPage<MovieShowTimeList> movieShowTimeList(Page<MovieShowTime> page, MovieShowTimeListQuery query);
}