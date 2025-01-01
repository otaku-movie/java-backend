package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Dict;
import com.example.backend.entity.MovieRate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieRateMapper extends BaseMapper<MovieRate> {
}