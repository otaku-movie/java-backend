package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieShowTimeTicketType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 场次限定票种规则
 */
@Mapper
public interface MovieShowTimeTicketTypeMapper extends BaseMapper<MovieShowTimeTicketType> {

  /** 按场次删除（保存时先删后插） */
  @Delete("DELETE FROM movie_show_time_ticket_type WHERE show_time_id = #{showTimeId}")
  int deleteByShowTimeId(@Param("showTimeId") Integer showTimeId);
}
