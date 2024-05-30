package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Api;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.response.MovieCommentResponse;
import com.example.backend.utils.Utils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class MovieCommentSaveQuery {
  Integer id;
  @NotNull
  @NotBlank(message = "content 不能为空")
  String content;
  @NotNull
  Integer movieId;
}

@RestController
public class MovieCommentController {
  @Autowired
  private MovieCommentMapper movieCommentMapper;

  @PostMapping("/api/movie/comment/list")
  public RestBean<List<MovieCommentResponse>> list(@RequestBody MovieCommentListQuery query)  {
    Page<MovieComment> page = new Page<>(query.getPage() - 1, query.getPageSize());

    IPage<MovieCommentResponse> list = movieCommentMapper.commentList(page, query);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/comment/detail")
  public RestBean<MovieComment> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<MovieComment> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieComment result = movieCommentMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/movie/comment/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    movieCommentMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
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

      return RestBean.success(null, "success");
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());

      movieCommentMapper.update(data, updateQueryWrapper);

      return RestBean.success(null, "success");
    }
  }
}
