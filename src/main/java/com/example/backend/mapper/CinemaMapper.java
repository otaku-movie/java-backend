package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Cinema;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
  void insert();

//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}