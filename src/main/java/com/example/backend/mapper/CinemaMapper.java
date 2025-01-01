package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.query.CinemaListQuery;
import com.example.backend.entity.Cinema;
import com.example.backend.query.GetCinemaMovieShowTimeListQuery;
import com.example.backend.response.CinemaResponse;
import com.example.backend.response.Spec;
import com.example.backend.response.app.AppMovieShowTimeResponse;
import com.example.backend.response.app.GetCinemaMovieShowTimeListResponse;
import com.example.backend.response.cinema.MovieShowingResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
  IPage<CinemaResponse> cinemaList(CinemaListQuery query, IPage<CinemaResponse> page);

  CinemaResponse cinemaDetail(Integer id);
  List<Spec> cinemaSpec(Integer cinemaId);
  List<MovieShowingResponse> getMovieShowing(Integer cinemaId);
  GetCinemaMovieShowTimeListResponse getCinemaMovieShowTimeList(
    GetCinemaMovieShowTimeListQuery query,
    Integer showTimeState
  );
}