package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Benefit;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieOrder;
import com.example.backend.entity.MovieRate;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.MovieTag;
import com.example.backend.entity.MovieTagTags;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.BenefitMapper;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieTagMapper;
import com.example.backend.mapper.MovieTagTagsMapper;
import com.example.backend.response.app.BenefitShareDetailResponse;
import com.example.backend.response.app.MovieShareDetailResponse;
import com.example.backend.response.app.OrderShareDetailResponse;
import com.example.backend.utils.MessageUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * App 端 - 分享落地页
 */
@RestController
public class AppShareController {

  @Autowired
  private MovieMapper movieMapper;
  @Autowired
  private MovieTagTagsMapper movieTagTagsMapper;
  @Autowired
  private MovieTagMapper movieTagMapper;
  @Autowired
  private MovieRateMapper movieRateMapper;
  @Autowired
  private MovieOrderMapper movieOrderMapper;
  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;
  @Autowired
  private CinemaMapper cinemaMapper;
  @Autowired
  private BenefitMapper benefitMapper;

  private static final ObjectMapper JSON = new ObjectMapper();

  @GetMapping(ApiPaths.App.Share.MOVIE_DETAIL)
  public RestBean<MovieShareDetailResponse> movieDetail(@PathVariable("id") Integer id) {
    if (id == null) {
      return RestBean.error(400, "Invalid movie id");
    }
    Movie movie = movieMapper.selectById(id);
    if (movie == null) {
      return RestBean.error(404, "Movie not found");
    }

    MovieShareDetailResponse data = new MovieShareDetailResponse();
    data.setId(movie.getId());
    data.setName(movie.getName());
    data.setOriginalName(movie.getOriginalName());
    data.setCover(movie.getCover());
    data.setDescription(movie.getDescription());
    data.setDuration(movie.getTime());
    data.setStartDate(movie.getStartDate());
    data.setStatus(movie.getStatus());
    data.setWatchedCount(movie.getWatchedCount());
    data.setWantToSeeCount(movie.getWantToSeeCount());
    data.setHomePage(movie.getHomePage());
    data.setTags(loadTagNames(id));

    MovieRateSummary summary = loadRatingSummary(id);
    data.setRating(summary.average);
    data.setRatingCount(summary.count);

    return RestBean.success(data, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  private List<String> loadTagNames(Integer movieId) {
    List<MovieTagTags> mappings = movieTagTagsMapper.selectList(new QueryWrapper<MovieTagTags>()
        .eq("movie_id", movieId)
        .eq("deleted", 0));
    if (mappings == null || mappings.isEmpty()) {
      return Collections.emptyList();
    }
    List<Integer> tagIds = mappings.stream()
        .map(MovieTagTags::getMovieTagId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
    if (tagIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<MovieTag> tags = movieTagMapper.selectList(new QueryWrapper<MovieTag>()
        .in("id", tagIds)
        .eq("deleted", 0));
    return tags.stream()
        .map(MovieTag::getName)
        .filter(name -> name != null && !name.isEmpty())
        .collect(Collectors.toList());
  }

  private MovieRateSummary loadRatingSummary(Integer movieId) {
    List<MovieRate> rates = movieRateMapper.selectList(new QueryWrapper<MovieRate>()
        .eq("movie_id", movieId));
    MovieRateSummary summary = new MovieRateSummary();
    if (rates == null || rates.isEmpty()) {
      summary.average = null;
      summary.count = 0;
      return summary;
    }
    double total = 0d;
    for (MovieRate rate : rates) {
      total += rate.getRate();
    }
    double avg = total / rates.size();
    summary.average = Math.round(avg * 10d) / 10d;
    summary.count = rates.size();
    return summary;
  }

  private static class MovieRateSummary {
    Double average;
    Integer count;
  }

  /**
   * 订单/票根分享落地页。
   * <p>不依赖登录态，但只返回「电影 + 场次时间 + 影院」这类朋友圈炫耀级别的字段，
   * 不返回座位号 / 支付金额 / 用户身份，避免 orderNumber 被分享后导致信息泄露。</p>
   */
  @GetMapping(ApiPaths.App.Share.ORDER_DETAIL)
  public RestBean<OrderShareDetailResponse> orderDetail(@PathVariable("orderNumber") String orderNumber) {
    if (orderNumber == null || orderNumber.isBlank()) {
      return RestBean.error(400, "Invalid order number");
    }
    MovieOrder order = movieOrderMapper.selectOne(new QueryWrapper<MovieOrder>()
        .eq("order_number", orderNumber)
        .eq("deleted", 0)
        .last("LIMIT 1"));
    if (order == null) {
      return RestBean.error(404, "Order not found");
    }
    OrderShareDetailResponse data = new OrderShareDetailResponse();
    data.setOrderNumber(orderNumber);

    if (order.getMovieShowTimeId() != null) {
      MovieShowTime showTime = movieShowTimeMapper.selectById(order.getMovieShowTimeId());
      if (showTime != null) {
        // start_time 是 PostgreSQL timestamp 类型，被 MyBatis 取出后是字符串形式如 "2026-06-12 18:30:00"，
        // 简单拆成 date + time 两段返回，避免前端再处理时区。
        String raw = showTime.getStartTime();
        if (raw != null && raw.contains(" ")) {
          String[] parts = raw.split(" ", 2);
          data.setDate(parts[0]);
          data.setStartTime(parts[1].length() >= 5 ? parts[1].substring(0, 5) : parts[1]);
        } else if (raw != null && raw.length() >= 10) {
          data.setDate(raw.substring(0, 10));
        }
        if (showTime.getMovieId() != null) {
          Movie movie = movieMapper.selectById(showTime.getMovieId());
          if (movie != null) {
            data.setMovieId(movie.getId());
            data.setMovieName(movie.getName());
            data.setMoviePoster(movie.getCover());
          }
        }
        if (showTime.getCinemaId() != null) {
          Cinema cinema = cinemaMapper.selectById(showTime.getCinemaId());
          if (cinema != null) {
            data.setCinemaName(cinema.getName());
            data.setCinemaCity(cinema.getAddress());
          }
        }
      }
    }
    return RestBean.success(data, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  /**
   * 入场者特典分享落地页。
   */
  @GetMapping(ApiPaths.App.Share.BENEFIT_DETAIL)
  public RestBean<BenefitShareDetailResponse> benefitDetail(
      @PathVariable("movieId") Integer movieId,
      @PathVariable("benefitId") Integer benefitId) {
    if (movieId == null || benefitId == null) {
      return RestBean.error(400, "Invalid path");
    }
    Benefit benefit = benefitMapper.selectById(benefitId);
    if (benefit == null || benefit.getMovieId() == null || !benefit.getMovieId().equals(movieId)) {
      // 强制 movieId 与 benefit 关联匹配，防止通过任意 movieId 探测他人特典。
      return RestBean.error(404, "Benefit not found");
    }

    BenefitShareDetailResponse data = new BenefitShareDetailResponse();
    data.setId(benefit.getId());
    data.setMovieId(benefit.getMovieId());
    data.setName(benefit.getName());
    data.setDescription(benefit.getDescription());
    data.setStartDate(benefit.getStartDate());
    data.setEndDate(benefit.getEndDate());
    data.setStatus(benefit.getPhaseStatus());
    data.setImageUrls(parseImageUrls(benefit.getImageUrls()));

    Movie movie = movieMapper.selectById(movieId);
    if (movie != null) {
      data.setMovieName(movie.getName());
      data.setMoviePoster(movie.getCover());
    }
    return RestBean.success(data, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  /** 特典物料图列表存的是 JSON 数组字符串，容错解析后返回。 */
  private List<String> parseImageUrls(String raw) {
    if (raw == null || raw.isBlank()) return Collections.emptyList();
    try {
      return JSON.readValue(raw, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
