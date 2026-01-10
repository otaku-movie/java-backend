package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieVersion;
import com.example.backend.response.movie.MovieVersionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电影版本Mapper
 */
@Mapper
public interface MovieVersionMapper extends BaseMapper<MovieVersion> {
    /**
     * 获取电影的所有版本信息（包含角色和演员）
     */
    List<MovieVersionResponse> getMovieVersions(@Param("movieId") Integer movieId);
}
