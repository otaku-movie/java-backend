package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.TheaterHall;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TheaterHallMapper extends BaseMapper<TheaterHall> {
}