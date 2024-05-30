package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.MovieReply;

import java.util.List;

/**
* @author last order
* @description 针对表【button】的数据库操作Mapper
* @createDate 2024-05-24 17:37:24
* @Entity com.example.backend.entity.TheaterHall.Button
*/
public interface MovieReplyMapper extends BaseMapper<MovieReply> {
  List<Object> replyList();
}




