package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.controller.CinemaListQuery;
import com.example.backend.entity.Cinema;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.CinemaResponse;
import com.example.backend.response.MovieResponse;
import org.apache.ibatis.annotations.Mapper;
import com.example.backend.response.Spec;

import java.util.List;


@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
  IPage<CinemaResponse> cinemaList(CinemaListQuery query, IPage<CinemaResponse> page);

  CinemaResponse cinemaDetail(Integer id);
  List<Object> cinemaSpec(Integer cinemaId);
}