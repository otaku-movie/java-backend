package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.query.MovieCommentListQuery;
import com.example.backend.response.MovieCommentResponse;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class MovieCommentSaveQuery {
  Integer id;
  @NotEmpty(message = "{validator.movieComment.content.required}")
  @Max(value = 1000, message =  "{validator.movieComment.content.size}")
  String content;
  @NotNull
  Integer movieId;
}

@RestController
public class MovieCommentController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieCommentMapper movieCommentMapper;

  @PostMapping("/api/movie/comment/list")
  public RestBean<List<MovieCommentResponse>> list(@RequestBody @Validated MovieCommentListQuery query)  {
    Page<MovieComment> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieCommentResponse> list = movieCommentMapper.commentList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/comment/detail")
  public RestBean<MovieComment> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<MovieComment> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieComment result = movieCommentMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @DeleteMapping("/api/movie/comment/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    movieCommentMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }

  @SaCheckLogin
  @PostMapping("/api/movie/comment/save")
  public RestBean<List<Object>> save(@RequestBody @Validated MovieCommentSaveQuery query) {
    MovieComment data = new MovieComment();

    data.setMovieId(query.getMovieId());
    data.setContent(query.getContent());
    data.setCommentUserId(Utils.getUserId());

    if (query.getId() == null) {
      movieCommentMapper.insert(data);

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());

      movieCommentMapper.update(data, updateQueryWrapper);

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    }
  }
}
