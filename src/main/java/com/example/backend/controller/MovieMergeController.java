package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.query.MovieDuplicateQuery;
import com.example.backend.query.MovieMergeDetailQuery;
import com.example.backend.query.MovieMergeQuery;
import com.example.backend.query.MovieMergeSearchQuery;
import com.example.backend.query.MoviePendingMatchResolveQuery;
import com.example.backend.response.movie.MovieDuplicateGroup;
import com.example.backend.response.movie.MovieDuplicateItem;
import com.example.backend.response.movie.MovieMergeDetail;
import com.example.backend.response.movie.MovieMergeResult;
import com.example.backend.response.movie.MoviePendingMatch;
import com.example.backend.service.MovieMergeService;
import com.example.backend.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台「电影去重合并」接口。
 *
 * <p>所有接口 {@code @SaCheckLogin + @CheckPermission(code = "movie.merge")}：
 * 权限码由 V67 迁移登记并绑定 system 超管角色。</p>
 */
@RestController
public class MovieMergeController {

  @Autowired
  private MovieMergeService movieMergeService;

  /** 自动模式：分页返回重复候选组。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @PostMapping(ApiPaths.Admin.Movie.DUPLICATES)
  public RestBean<List<MovieDuplicateGroup>> duplicates(@RequestBody MovieDuplicateQuery query) {
    long total = movieMergeService.countDuplicateGroups();
    List<MovieDuplicateGroup> list = movieMergeService.listDuplicateGroups(query);
    return RestBean.success(list, query.getPage(), total, query.getPageSize());
  }

  /** 手动模式：按关键词搜索可合并电影。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @PostMapping(ApiPaths.Admin.Movie.MERGE_SEARCH)
  public RestBean<List<MovieDuplicateItem>> mergeSearch(@RequestBody MovieMergeSearchQuery query) {
    List<MovieDuplicateItem> list = movieMergeService.searchMovies(query.getKeyword());
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Success.GET));
  }

  /** 详情对比：批量拉取候选电影完整信息（用于合并前并排核对）。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @PostMapping(ApiPaths.Admin.Movie.MERGE_DETAIL)
  public RestBean<List<MovieMergeDetail>> mergeDetail(@RequestBody @Validated MovieMergeDetailQuery query) {
    List<MovieMergeDetail> list = movieMergeService.mergeDetails(query.getIds());
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Success.GET));
  }

  /** 待确认模式：crawler 写入的灰区模糊匹配。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @PostMapping(ApiPaths.Admin.Movie.PENDING_MATCHES)
  public RestBean<List<MoviePendingMatch>> pendingMatches(@RequestBody MovieDuplicateQuery query) {
    long total = movieMergeService.countPendingMatches();
    List<MoviePendingMatch> list = movieMergeService.listPendingMatches(query);
    return RestBean.success(list, query.getPage(), total, query.getPageSize());
  }

  /** 执行合并。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @Transactional(rollbackFor = Exception.class)
  @PostMapping(ApiPaths.Admin.Movie.MERGE)
  public RestBean<MovieMergeResult> merge(@RequestBody @Validated MovieMergeQuery query) {
    if (query.getSurvivorId() == null || query.getLoserIds() == null || query.getLoserIds().isEmpty()) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.PARAMETER));
    }
    MovieMergeResult result = movieMergeService.merge(query);
    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Success.SAVE));
  }

  /** 合并或忽略一条待确认匹配。 */
  @SaCheckLogin
  @CheckPermission(code = "movie.merge")
  @Transactional(rollbackFor = Exception.class)
  @PostMapping(ApiPaths.Admin.Movie.PENDING_MATCH_RESOLVE)
  public RestBean<MovieMergeResult> resolvePending(
      @RequestBody @Validated MoviePendingMatchResolveQuery query) {
    MovieMergeResult result = movieMergeService.resolvePending(query);
    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Success.SAVE));
  }
}
