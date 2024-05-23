package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Movie;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.MovieResponse;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    IPage<MovieResponse> movieList(MovieListQuery query, IPage<MovieResponse> page);
    MovieResponse movieDetail(Integer id);
//    IPage<Movie> selectList(Page<Object> page, QueryWrapper wrapper);
}