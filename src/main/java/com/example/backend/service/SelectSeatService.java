package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.SeatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author last order
* @description 针对表【api】的数据库操作Service
* @createDate 2024-05-24 17:37:24
*/

@Service
public class SelectSeatService extends ServiceImpl<SelectSeatMapper, SelectSeat> {
  @Autowired
  SeatMapper seatMapper;
  @Autowired
  SeatAreaMapper seatAreaMapper;
  @Autowired
  SeatAisleMapper seatAisleMapper;
  @Autowired
  SeatAreaService seatAreaService;
  @Autowired
  SeatAisleService seatAisleService;
  @Autowired
  CinemaMapper cinemaMapper;
  @Autowired
  TheaterHallMapper theaterHallMapper;

  @Autowired
  SelectSeatMapper selectSeatMapper;
  @Autowired
  SeatService seatService;

  public Object selectSeatList(Integer theaterHallId, Integer movieShowTimeId) {
    List<SeatListResponse> seatList = selectSeatMapper.selectSeatList(theaterHallId, movieShowTimeId);

    List result = new ArrayList();

    Map<Integer, List<SeatListResponse>> map = seatList.stream().collect(
      Collectors.groupingBy(SeatListResponse::getX)
    );

    map.forEach((row, seat) -> {
      SeatResponse modal = new SeatResponse();
      modal.setRowAxis(row);
      modal.setChildren(seat);

      result.add(modal);
    });

    // 查询影院最大选座数量
    TheaterHall theaterHall = theaterHallMapper.selectById(theaterHallId);
    Cinema cinema = cinemaMapper.selectById(theaterHall.getCinemaId());

    SeatDetailResponse seatDetailResponse = new SeatDetailResponse();
    seatDetailResponse.setMaxSelectSeatCount(cinema.getMaxSelectSeatCount());
    seatDetailResponse.setAisle(seatService.getSeatAisle(theaterHallId));
    seatDetailResponse.setSeat(result);
    seatDetailResponse.setArea(seatService.getSeatArea(theaterHallId));

    return seatDetailResponse;
  }
}
