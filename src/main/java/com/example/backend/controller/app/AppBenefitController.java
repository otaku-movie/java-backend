package com.example.backend.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.query.benefit.BenefitFeedbackSubmitQuery;
import com.example.backend.query.benefit.BenefitMovieListQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.response.benefit.BenefitCinemaAvailabilityItemResponse;
import com.example.backend.response.benefit.BenefitDetailResponse;
import com.example.backend.response.benefit.BenefitMovieListItemResponse;
import com.example.backend.service.BenefitService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App 端 - 入场者特典（C 端查看某电影的特典列表；用户反馈需登录）
 */
@RestController
@Validated
public class AppBenefitController {

  @Autowired
  private BenefitService benefitService;

  /** 按电影分组的特典入口列表（匿名）：给 H5/App 的「特典」Tab 使用。 */
  @GetMapping(ApiPaths.App.Benefit.MOVIE_LIST)
  public RestBean<List<BenefitMovieListItemResponse>> movieList(@ModelAttribute BenefitMovieListQuery query) {
    if (query == null) query = new BenefitMovieListQuery();
    int ps = query.getPageSize() != null && query.getPageSize() > 0
      ? Math.min(query.getPageSize(), 100)
      : 20;
    query.setPage(query.getPage() != null && query.getPage() > 0 ? query.getPage() : 1);
    query.setPageSize(ps);
    IPage<BenefitMovieListItemResponse> page = benefitService.listBenefitMoviesForAdmin(query);
    return RestBean.success(page.getRecords(), query.getPage(), page.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.App.Benefit.LIST)
  public RestBean<List<BenefitDetailResponse>> list(@RequestParam Integer movieId,
                                                    @RequestParam(required = false) Integer reReleaseId) {
    if (movieId == null) {
      return RestBean.success(List.of(), MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
    List<BenefitDetailResponse> list = benefitService.listBenefitDetailByMovie(movieId, reReleaseId);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /** 按特典分页查询可领影院（匿名） */
  @GetMapping(ApiPaths.App.Benefit.CINEMA_AVAILABILITY)
  public RestBean<List<BenefitCinemaAvailabilityItemResponse>> cinemaAvailability(
    @PathVariable("benefitId") Integer benefitId,
    @RequestParam(required = false) Integer reReleaseId,
    @RequestParam(required = false) Integer regionId,
    @RequestParam(required = false) Integer prefectureId,
    @RequestParam(required = false) Integer cityId,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false, defaultValue = "remainingDesc") String sort,
    @RequestParam(required = false) Double latitude,
    @RequestParam(required = false) Double longitude,
    @RequestParam(required = false, defaultValue = "1") Integer page,
    @RequestParam(required = false, defaultValue = "20") Integer pageSize
  ) {
    int p = page != null && page > 0 ? page : 1;
    int ps = pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20;
    Integer currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
    IPage<BenefitCinemaAvailabilityItemResponse> result = benefitService.pageCinemasForBenefitApp(
      benefitId, reReleaseId, regionId, prefectureId, cityId, keyword, sort, latitude, longitude, p, ps, currentUserId);
    return RestBean.success(result.getRecords(), (int) result.getCurrent(), result.getTotal(), (int) result.getSize());
  }

  /** 用户反馈：当前影院该特典物料已领完等（需登录） */
  @SaCheckLogin
  @PostMapping(ApiPaths.App.Benefit.FEEDBACK_SUBMIT)
  public RestBean<String> submitFeedback(@Valid @RequestBody BenefitFeedbackSubmitQuery query) {
    Integer userId = StpUtil.getLoginIdAsInt();
    benefitService.submitFeedback(
      userId,
      query.getCinemaId(),
      query.getBenefitId(),
      query.getFeedbackType() != null ? query.getFeedbackType() : 1);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
}
