package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.MovieReply;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.CommentEnumType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieReplyMapper;
import com.example.backend.query.MovieReplyListQuery;
import com.example.backend.response.MovieReplyResponse;
import com.example.backend.response.comment.CommentReactionData;
import com.example.backend.service.MovieCommentService;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class MovieReplySaveQuery {
  Integer id;
  @Size(max = 1000, message ="{validator.movieReply.content.required}")
  @NotEmpty(message = "{validator.movieReply.content.required}")
  String content;
  @NotNull
  Integer movieCommentId;
  @NotNull
  Integer movieId;
  String parentReplyId;
  //  @NotNull
  Integer commentUserId;
  Integer replyUserId;
}

@RestController
public class MovieReplyController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieReplyMapper movieReplyMapper;

  @Autowired
  MovieCommentService movieCommentService;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping("/api/movie/reply/list")
  public RestBean<List<MovieReplyResponse>> list(@RequestBody MovieReplyListQuery query)  {
    Page<MovieReply> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieReplyResponse> list = movieReplyMapper.replyList(query, page);

    List<MovieReplyResponse> result = list.getRecords().stream().map(item -> {
      // 读取redis的评论和用户是否点赞
      CommentReactionData data = movieCommentService.getRedisData(CommentEnumType.reply.getCode(), item.getId());
      item.setDislike(data.isDislike());
      item.setLike(data.isLike());
      item.setLikeCount(data.getLikeCount());
      item.setDislikeCount(data.getDislikeCount());

      return  item;
    }).toList();

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping("/api/movie/reply/detail")
  public RestBean<Object> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<MovieReply> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieReplyResponse result = movieReplyMapper.selectOne(queryWrapper);
    // 读取redis的评论和用户是否点赞
    CommentReactionData data = movieCommentService.getRedisData(CommentEnumType.reply.getCode(), result.getId());
    result.setDislike(data.isDislike());
    result.setLike(data.isLike());
    result.setLikeCount(data.getLikeCount());
    result.setDislikeCount(data.getDislikeCount());

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "reply.remove")
  @DeleteMapping("/api/movie/reply/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    movieReplyMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }

  @SaCheckLogin
  @PostMapping("/api/movie/reply/save")
  public RestBean<List<Object>> save(@RequestBody @Validated MovieReplySaveQuery query) {
    MovieReply data = new MovieReply();

    data.setMovieCommentId(query.getMovieCommentId());
    data.setContent(query.getContent());
    data.setMovieId(query.getMovieId());
    data.setParentReplyId(query.getParentReplyId());
    data.setCommentUserId(Utils.getUserId());

    if (query.getParentReplyId().split("-").length > 1) {
      data.setReplyUserId(query.getReplyUserId());

      movieReplyMapper.insert(data);
      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } else {
      data.setCommentUserId(Utils.getUserId());
      // 创建
      if (query.getId() == null) {
        movieReplyMapper.insert(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        // 编辑
        data.setId(query.getId());
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());

        movieReplyMapper.update(data, updateQueryWrapper);

        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      }
    }
  }
  @SaCheckLogin
  @PostMapping("/api/movie/reply/like")
  public RestBean<Boolean> like(@RequestBody MovieCommentActionQuery query) {
    Boolean result = movieCommentService.toggleAction(
      CommentEnumType.reply.getCode(),
      query.getId(), true
    );

    return RestBean.success(result, MessageUtils.getMessage("success.action"));
  }
  @SaCheckLogin
  @PostMapping("/api/movie/reply/dislike")
  public RestBean<Boolean> dislike(@RequestBody MovieCommentActionQuery query) {
    Boolean result = movieCommentService.toggleAction(
      CommentEnumType.reply.getCode(),
      query.getId(), false
    );

    return RestBean.success(result, MessageUtils.getMessage("success.action"));
  }
}
