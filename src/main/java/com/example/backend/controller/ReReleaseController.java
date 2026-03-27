package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.ReReleaseMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.reRelease.ReReleaseListResponse;
import com.example.backend.utils.MessageUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
class ReReleaseSaveQuery {
  private Integer id;

  @NotNull(message = "{validator.movieComment.movieId.required}")
  private Integer movieId;

  /** yyyy-MM-dd */
  @NotBlank(message = "{validator.showTime.startDate.required}")
  private String startDate;
  /** yyyy-MM-dd，可为空表示长期 */
  private String endDate;
  private Integer status;
  private String versionInfo;
  private String displayNameOverride;
  private String posterOverride;
  /** 覆盖片长（分钟，可选） */
  private Integer timeOverride;
}

@RestController
public class ReReleaseController {
  @Autowired
  private ReReleaseMapper reReleaseMapper;

  @Resource
  RedisTemplate<String, Object> redisTemplate;

  @PostMapping(ApiPaths.Common.ReRelease.LIST)
  public RestBean<List<ReReleaseListResponse>> list(@RequestBody @Validated MovieListQuery query)  {
    Page<ReRelease> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<ReReleaseListResponse> list = reReleaseMapper.reReleaseList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.ReRelease.DETAIL)
  public RestBean<ReRelease> detail (@RequestParam("id") Integer id) {
    if (id == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    ReRelease rr = reReleaseMapper.selectById(id);
    return rr != null
      ? RestBean.success(rr, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS))
      : RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
  }
  @SaCheckLogin
  @DeleteMapping(ApiPaths.Common.ReRelease.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {

    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    reReleaseMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Admin.Movie.RE_RELEASE_SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated ReReleaseSaveQuery query) {
    Date startDateParsed;
    Date endDateParsed = null;
    try {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      fmt.setLenient(false);
      startDateParsed = fmt.parse(query.getStartDate());
      if (query.getEndDate() != null && !query.getEndDate().isBlank()) {
        endDateParsed = fmt.parse(query.getEndDate());
      }
      if (endDateParsed != null && !startDateParsed.before(endDateParsed)) {
        return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
      }
    } catch (Exception e) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }

    ReRelease modal = new ReRelease();
    modal.setId(query.getId());
    modal.setMovieId(query.getMovieId());
    modal.setStartDate(startDateParsed);
    modal.setEndDate(endDateParsed);
    modal.setStatus(query.getStatus() != null ? query.getStatus() : 1);
    modal.setVersionInfo(query.getVersionInfo());
    modal.setDisplayNameOverride(query.getDisplayNameOverride());
    modal.setPosterOverride(query.getPosterOverride());
    modal.setTimeOverride(query.getTimeOverride());

    if (query.getId() == null) reReleaseMapper.insert(modal);
    else reReleaseMapper.updateById(modal);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
  }
}
