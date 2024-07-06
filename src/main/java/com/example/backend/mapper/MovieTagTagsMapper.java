package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Api;
import com.example.backend.entity.MovieTagTags;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
* @author last order
* @description 针对表【api】的数据库操作Mapper
* @createDate 2024-05-24 17:37:24
* @Entity com.example.backend.entity.TheaterHall.Api
*/
public interface MovieTagTagsMapper extends BaseMapper<MovieTagTags> {
  @Delete("DELETE FROM movie_tag_tags WHERE movie_id = #{movieId}")
  void deleteMovieTags(@Param("movieId") Integer movieId);
}




