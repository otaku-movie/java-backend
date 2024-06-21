package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Seat;
import com.example.backend.entity.TheaterHall;
import com.example.backend.query.SeatQuery;
import com.example.backend.response.SeatListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
  void deleteSeat(Integer id);
  List<SeatListResponse> seatList(Integer theaterHallId);
}