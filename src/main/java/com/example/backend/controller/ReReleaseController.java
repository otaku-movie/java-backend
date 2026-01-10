package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.*;
import com.example.backend.enumerate.CommentEnumType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.mapper.MovieReplyMapper;
import com.example.backend.mapper.ReReleaseMapper;
import com.example.backend.query.MovieCommentListQuery;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.MovieReplyListQuery;
import com.example.backend.response.MovieReplyResponse;
import com.example.backend.response.comment.CommentDetail;
import com.example.backend.response.comment.CommentReactionData;
import com.example.backend.response.reRelease.ReReleaseListResponse;
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

import java.util.Date;
import java.util.List;

@Data
class ReReleaseSaveQuery {
  Integer id;

  @NotNull(message = "{validator.movieComment.movieId.required}")
  Integer movieId;


  Date startDate;
  Date endDate;
}

@RestController
public class ReReleaseController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private ReReleaseMapper reReleaseMapper;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping(ApiPaths.Common.ReRelease.LIST)
  public RestBean<List<ReReleaseListResponse>> list(@RequestBody @Validated MovieListQuery query)  {
    Page<ReRelease> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<ReReleaseListResponse> list = reReleaseMapper.reReleaseList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.ReRelease.DETAIL)
  public RestBean<CommentDetail> detail (@RequestParam("id") Integer id) {

    return null;
  }
  @SaCheckLogin
  @DeleteMapping(ApiPaths.Common.ReRelease.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {

    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    reReleaseMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Movie.RE_RELEASE_SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated ReReleaseSaveQuery query) {
    if (query.getId() == null) {
      ReRelease modal = new ReRelease();
      modal.setId(query.getId());
      modal.setMovieId(query.getMovieId());
      modal.setStartDate(query.getStartDate());
      modal.setEndDate(query.getEndDate());

      reReleaseMapper.insert(modal);
    }

    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
}
