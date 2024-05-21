package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.MovieShowTime;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface MovieShowTimeMapper extends BaseMapper<MovieShowTime> {
  List movieShowTimeList();

//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}