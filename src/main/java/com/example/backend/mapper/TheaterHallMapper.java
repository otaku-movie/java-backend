package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.controller.TheaterHallQuery;
import com.example.backend.entity.TheaterHall;
import com.example.backend.response.TheaterHallList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface TheaterHallMapper extends BaseMapper<TheaterHall> {
  IPage<TheaterHallList> theaterHallList(TheaterHallQuery query, Page<TheaterHall> page);
  
  /**
   * 查询影厅座位数
   * @param theaterHallId 影厅ID
   * @return 座位数
   */
  Integer getSeatCount(@Param("theaterHallId") Integer theaterHallId);
}