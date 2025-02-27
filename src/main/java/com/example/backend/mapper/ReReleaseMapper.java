package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Movie;
import com.example.backend.entity.ReRelease;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.app.AppMovieListQuery;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.MovieStaffResponse;
import com.example.backend.response.Spec;
import com.example.backend.response.app.AppBeforeMovieShowTimeResponse;
import com.example.backend.response.app.AppMovieStaffResponse;
import com.example.backend.response.app.NowMovieShowingResponse;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.response.movie.Tags;
import com.example.backend.response.reRelease.ReReleaseListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface ReReleaseMapper extends BaseMapper<ReRelease> {
    IPage<ReReleaseListResponse> reReleaseList(MovieListQuery query, IPage<ReRelease> page);
}