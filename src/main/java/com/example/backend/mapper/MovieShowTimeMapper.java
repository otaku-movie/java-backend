package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.response.MovieShowTimeList;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MovieShowTimeMapper extends BaseMapper<MovieShowTime> {
  IPage<MovieShowTimeList> movieShowTimeList(Page<MovieShowTime> page, MovieShowTimeListQuery query);

//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}