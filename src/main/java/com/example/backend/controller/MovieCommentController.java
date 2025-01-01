package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.MovieRate;
import com.example.backend.entity.MovieReply;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.CommentEnumType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.mapper.MovieReplyMapper;
import com.example.backend.query.MovieCommentListQuery;
import com.example.backend.query.MovieReplyListQuery;
import com.example.backend.response.MovieCommentResponse;
import com.example.backend.response.MovieReplyResponse;
import com.example.backend.response.comment.CommentDetail;
import com.example.backend.response.comment.CommentReactionData;
import com.example.backend.service.MovieCommentService;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.backend.enumerate.RedisType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
class MovieCommentSaveQuery {
  Integer id;

  @NotEmpty(message = "{validator.movieComment.content.required}")
  @Size(max = 1000, message = "{validator.movieComment.content.max}")
  String content;

  @NotNull(message = "{validator.movieComment.movieId.required}")
  Integer movieId;


  @DecimalMin(value = "0.1", message = "{validator.movieComment.rate.min}")
  @DecimalMax(value = "10.0", message = "{validator.movieComment.rate.max}")
  Double rate;
}

@Data
class MovieCommentActionQuery {
  @NotNull
  Integer id;
}

@RestController
public class MovieCommentController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieCommentMapper movieCommentMapper;
  @Autowired
  private MovieReplyMapper movieReplyMapper;

  @Autowired
  MovieCommentService movieCommentService;

  @Autowired
  MovieRateMapper movieRateMapper;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping("/api/movie/comment/list")
  public RestBean<List<CommentDetail>> list(@RequestBody @Validated MovieCommentListQuery query)  {
    Page<MovieComment> page = new Page<>(query.getPage(), query.getPageSize());
    Page<MovieReply> replyPage = new Page<>(1, 3);

    IPage<CommentDetail> list = movieCommentMapper.commentList(query, page);

    List<CommentDetail> result = list.getRecords().stream().map(item -> {
      MovieReplyListQuery movieReplyListQuery = new MovieReplyListQuery();
      movieReplyListQuery.setCommentId(item.getId());
      IPage<MovieReplyResponse> replyList = movieReplyMapper.replyList(movieReplyListQuery, replyPage);

      // 读取redis的评论和用户是否点赞
      CommentReactionData data = movieCommentService.getRedisData(CommentEnumType.comment.getCode(), item.getId());
      item.setDislike(data.isDislike());
      item.setLike(data.isLike());
      item.setLikeCount(data.getLikeCount());
      item.setDislikeCount(data.getDislikeCount());

      item.setReply(replyList.getRecords());

      return  item;
    }).toList();

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/comment/detail")
  public RestBean<CommentDetail> detail (@RequestParam("id") Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    CommentDetail result = movieCommentMapper.commentDetail(id);

    CommentReactionData data = movieCommentService.getRedisData(CommentEnumType.comment.getCode(), result.getId());
    result.setDislike(data.isDislike());
    result.setLike(data.isLike());
    result.setLikeCount(data.getLikeCount());
    result.setDislikeCount(data.getDislikeCount());

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "comment.remove")
  @DeleteMapping("/api/movie/comment/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    movieCommentMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }

  @SaCheckLogin
  @Transactional
  @PostMapping("/api/movie/comment/save")
  public RestBean<List<Object>> save(@RequestBody @Validated MovieCommentSaveQuery query) {
    MovieComment data = new MovieComment();
    MovieRate movieRate = new MovieRate();
    data.setMovieId(query.getMovieId());
    data.setContent(query.getContent());
    data.setCommentUserId(Utils.getUserId());

    movieRate.setMovieId(query.getMovieId());
    movieRate.setUserId(Utils.getUserId());
    movieRate.setRate(query.getRate());

    QueryWrapper movieRateQueryWrapper = new QueryWrapper();
    movieRateQueryWrapper.eq("user_id", Utils.getUserId());
    movieRateQueryWrapper.eq("movie_id", query.getMovieId());

    MovieRate movieRateResult = movieRateMapper.selectOne(movieRateQueryWrapper);

    if (movieRateResult != null) {
      return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage("error.comment.userRated"));
    }

    if (query.getId() == null) {
      movieCommentMapper.insert(data);
      movieRateMapper.insert(movieRate);

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());

      movieCommentMapper.update(data, updateQueryWrapper);

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    }
  }
  @SaCheckLogin
  @PostMapping("/api/movie/comment/like")
  public RestBean<Boolean> like(@RequestBody MovieCommentActionQuery query) {
    Boolean result = movieCommentService.toggleAction(
      CommentEnumType.comment.getCode(),
      query.getId(), true
    );

    return RestBean.success(result, MessageUtils.getMessage("success.action"));
  }
  @SaCheckLogin
  @PostMapping("/api/movie/comment/dislike")
  public RestBean<Boolean> dislike(@RequestBody MovieCommentActionQuery query) {
    Boolean result = movieCommentService.toggleAction(
      CommentEnumType.comment.getCode(),
      query.getId(), false
    );

    return RestBean.success(result, MessageUtils.getMessage("success.action"));
  }
  @PostMapping("/api/movie/comment/syncLikeAndDislikeToDatabase")
  public RestBean<Null> syncLikeAndDislikeToDatabase() {
    movieCommentService.syncCommentLikeAndDislike();

    return RestBean.success(null, MessageUtils.getMessage("success.action"));
  }
}
