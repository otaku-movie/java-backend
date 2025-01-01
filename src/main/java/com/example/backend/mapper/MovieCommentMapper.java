package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.query.MovieCommentListQuery;
import com.example.backend.entity.MovieComment;
import com.example.backend.response.MovieCommentResponse;
import com.example.backend.response.comment.CommentDetail;

/**
* @author last order
* @description 针对表【button】的数据库操作Mapper
* @createDate 2024-05-24 17:37:24
* @Entity com.example.backend.entity.TheaterHall.Button
*/
public interface MovieCommentMapper extends BaseMapper<MovieComment> {
  IPage<CommentDetail> commentList(MovieCommentListQuery query, Page page);
   CommentDetail commentDetail(Integer id);
}




