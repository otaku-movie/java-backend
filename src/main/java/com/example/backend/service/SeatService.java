package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.controller.TheaterHallSaveQuery;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.query.SaveSeatQuery;
import com.example.backend.query.SeatAreaQuery;
import com.example.backend.query.SeatQuery;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.SeatResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author last order
* @description 针对表【api】的数据库操作Service
* @createDate 2024-05-24 17:37:24
*/

@Data
class SeatDetailResponse {
  Integer maxSelectSeatCount;
  List<SeatResponse> seat;
  List<SeatAisle> aisle;
  List<SeatArea> area;
}

@Service
public class SeatService extends ServiceImpl<SeatMapper, Seat> {
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

  @Transactional
  public void saveSeat(SaveSeatQuery query) {
    QueryWrapper queryWrapper = new QueryWrapper();

    queryWrapper.eq("theater_hall_id", query.getTheaterHallId());

    seatMapper.delete(queryWrapper);
    seatAisleMapper.delete(queryWrapper);
    seatAreaMapper.delete(queryWrapper);

    TheaterHall theaterHall = new TheaterHall();
    theaterHall.setId(query.getTheaterHallId());
    theaterHall.setSeatNamingRules(query.getSeatNamingRules());
    theaterHallMapper.updateById(theaterHall);

    List<SeatArea> seatAreaList = query.getArea().stream().map(item -> {
      SeatArea seatArea = new SeatArea();
//      seatArea.setId(item.getId());
      seatArea.setTheaterHallId(query.getTheaterHallId());
      seatArea.setName(item.getName());
      seatArea.setColor(item.getColor());
      seatArea.setPrice(item.getPrice());

      return seatArea;
    }).toList();

    seatAreaService.saveBatch(seatAreaList);

//    Map<String, Object> map = query.getArea().stream().collect(
//      Collectors.reducing((prev, current) -> {
//        current.getSeat().stream().collect()
//      })
//    );
    Map<String, Object> seatAreaMap = seatAreaList.stream().collect(Collectors.toMap(key -> key.getName(), v -> v));
    Map<String, SeatArea> map = new HashMap<>();

    for (SeatAreaQuery current : query.getArea()) {
      for (String seat: current.getSeat()) {

        map.put(seat, (SeatArea) seatAreaMap.get(current.getName()));
      }
    }
    List<Seat> seatList = query.getSeat().stream().map(item -> {
      Seat seat = new Seat();

      seat.setXAxis(item.getX());
      seat.setYAxis(item.getY());
      seat.setSeatPositionGroup(item.getSeatPositionGroup());
      seat.setShow(item.getShow());
      seat.setDisabled(item.getDisabled());
      seat.setWheelChair(item.getWheelChair());
      seat.setTheaterHallId(query.getTheaterHallId());
      seat.setSeatName(item.getSeatName());
      seat.setRowName(item.getRowName());

      String position = item.getX() + "-" + item.getY();
      SeatArea area = map.get(position);

      if (area != null) {
        seat.setSeatAreaId(area.getId());
      }

      return  seat;
    }).toList();

    List<SeatAisle> seatAisleList = query.getAisle().stream().map(item -> {
      SeatAisle seatAisle = new SeatAisle();

      seatAisle.setTheaterHallId(query.getTheaterHallId());
      seatAisle.setType(item.getType());
      seatAisle.setStart(item.getStart());

      return seatAisle;
    }).toList();

    this.saveBatch(seatList);

    seatAisleService.saveBatch(seatAisleList);
  }
  public List<SeatAisle> getSeatAisle(Integer theaterHallId) {
    QueryWrapper queryWrapper = new QueryWrapper();

    queryWrapper.eq("theater_hall_id", theaterHallId);

    return seatAisleMapper.selectList(queryWrapper);
  }
  public List<SeatArea> getSeatArea(Integer theaterHallId) {
    QueryWrapper queryWrapper = new QueryWrapper();

    queryWrapper.eq("theater_hall_id", theaterHallId);

    return seatAreaMapper.selectList(queryWrapper);
  }
  public Object seatList(Integer theaterHallId) {
    List<SeatListResponse> seatList = seatMapper.seatList(theaterHallId);

    List result = new ArrayList();

    Map<Integer, List<SeatListResponse>> map = seatList.stream().collect(
      Collectors.groupingBy(SeatListResponse::getX)
    );

    map.forEach((row, seat) -> {
      SeatResponse modal = new SeatResponse();
      modal.setRowAxis(row);
      modal.setRowName(seat.get(0).getRowName());
      modal.setChildren(seat);

      result.add(modal);
    });

    // 查询影院最大选座数量
    TheaterHall theaterHall = theaterHallMapper.selectById(theaterHallId);
    Cinema cinema = cinemaMapper.selectById(theaterHall.getCinemaId());

    SeatDetailResponse seatDetailResponse = new SeatDetailResponse();
    seatDetailResponse.setMaxSelectSeatCount(cinema.getMaxSelectSeatCount());
    seatDetailResponse.setAisle(getSeatAisle(theaterHallId));
    seatDetailResponse.setSeat(result);
    seatDetailResponse.setArea(getSeatArea(theaterHallId));


    return seatDetailResponse;
  }
}
