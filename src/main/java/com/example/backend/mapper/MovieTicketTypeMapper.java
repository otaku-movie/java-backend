package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.Position;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author last order
* @description 针对表【position】的数据库操作Mapper
* @createDate 2024-05-27 10:28:55
* @Entity generator.domain.Position
*/
public interface MovieTicketTypeMapper extends BaseMapper<MovieTicketType> {

  @Select("SELECT COALESCE(MAX(order_num), -1) FROM movie_ticket_type WHERE cinema_id = #{cinemaId} AND deleted = 0")
  Integer selectMaxOrderNumByCinemaId(@Param("cinemaId") Integer cinemaId);
}




