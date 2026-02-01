package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.SelectSeat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SelectSeatMapper extends BaseMapper<SelectSeat> {

  List selectSeatList(
    Integer theaterHallId,
    Integer movieShowTimeId,
    Integer userId,
    Integer selected
  );
  void  deleteSeat(Integer movieShowTimeId, Integer theaterHallId, Integer userId, List<Integer> x, List<Integer> y);
  
  /**
   * 通过座位ID删除选座信息
   */
  void deleteSeatBySeatId(Integer movieShowTimeId, Integer theaterHallId, Integer userId, List<Integer> seatIds);
  
  /**
   * 统计指定坐标的座位数量（并发控制）
   */
  Integer countSeatsByCoordinates(Integer movieShowTimeId, Integer theaterHallId, List<Integer> x, List<Integer> y, Integer userId);
}