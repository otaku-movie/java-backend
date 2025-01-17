package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.app.AppMovieListQuery;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.Spec;
import com.example.backend.response.app.*;
import com.example.backend.response.app.AppMovieShowTimeResponse;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.response.MovieStaffResponse;
import com.example.backend.response.movie.Tags;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    Integer getAllCinemaCount(Integer movieId);
    Integer getAllTheaterCount(Integer movieId);
    Integer getMovieCommentCount(Integer movieId);
    List<HelloMovie> getHelloMovie(Integer movieId);
    List<Spec> getMovieSpec(Integer movieId);
    // 根据电影id 获取电影标签
    List<Tags> getMovieTags(Integer movieId);
    IPage<MovieResponse> movieList(MovieListQuery query, IPage<MovieResponse> page);
    MovieResponse movieDetail(Integer id);
    MovieResponse getMovieRate(Integer id);
    // 热映电影
    IPage<NowMovieShowingResponse> nowMovieShowing(AppMovieListQuery query, IPage<MovieMapper> page);
    List<AppMovieStaffResponse> appMovieStaff(Integer movieId);
    IPage<Movie> getMovieComingSoon(AppMovieListQuery query, IPage<MovieMapper> page);

    List<MovieStaffResponse> movieStaffList(Integer id);

    List<MovieStaffResponse> movieCharacterList(Integer id);

    List<AppBeforeMovieShowTimeResponse> getMovieShowTime(
      getMovieShowTimeQuery query,
      Integer showTimeState,
      IPage<MovieShowTimeMapper> page
    );


//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}