package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.MovieSpec;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MovieSpecMapper extends BaseMapper<MovieSpec> {
  void deleteSpec(Integer id);
}