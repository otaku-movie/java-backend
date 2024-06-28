package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.SelectSeat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SelectSeatMapper extends BaseMapper<SelectSeat> {

  List selectSeatList(Integer theaterHallId, Integer movieShowTimeId, Integer userId, Integer selectSeatState);
  void  deleteSeat(Integer movieShowTimeId, Integer theaterHallId, Integer userId, List<Integer> x, List<Integer> y);
}