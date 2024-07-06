package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Movie;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.response.MovieStaffResponse;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    IPage<MovieResponse> movieList(MovieListQuery query, IPage<MovieResponse> page);
    MovieResponse movieDetail(Integer id);

    List<MovieStaffResponse> movieStaffList(Integer id);

    List<MovieStaffResponse> movieCharacterList(Integer id);
//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}