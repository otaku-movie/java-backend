package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieRate;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.response.app.HomeHighlightItem;
import com.example.backend.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C 端官网首页相关接口
 */
@RestController
public class AppHomeController {

  @Autowired
  private MovieMapper movieMapper;
  @Autowired
  private MovieRateMapper movieRateMapper;

  private static final int HIGHLIGHT_LIMIT = 3;
  private static final int HIGHLIGHT_FETCH_BATCH = 24;

  /**
   * 首屏精选海报：优先「热门排片」（未来场次最多 + 有海报），
   * 不够时依次按「上映中 / 即将上映 / 任意有海报」的想看人数兜底。
   */
  @GetMapping(ApiPaths.App.Home.HIGHLIGHTS)
  public RestBean<List<HomeHighlightItem>> highlights() {
    List<Movie> candidates = new ArrayList<>();

    // 1) 热门排片：未来已公开场次最多的电影（多取一批，后续 Java 层再过滤/截断）
    candidates.addAll(movieMapper.homeHighlightByShowTime(HIGHLIGHT_FETCH_BATCH));

    // 2) 不够则补「上映中 + 有 cover」
    if (presentableCount(candidates) < HIGHLIGHT_LIMIT) {
      candidates.addAll(fetchMovies(excludeExisting(candidates, new QueryWrapper<Movie>()
          .eq("status", 2)
          .isNotNull("cover")
          .ne("cover", "")
          .orderByDesc("want_to_see_count")
          .last("limit " + HIGHLIGHT_FETCH_BATCH))));
    }

    // 3) 不够则补「即将上映」
    if (presentableCount(candidates) < HIGHLIGHT_LIMIT) {
      candidates.addAll(fetchMovies(excludeExisting(candidates, new QueryWrapper<Movie>()
          .eq("status", 1)
          .isNotNull("cover")
          .ne("cover", "")
          .orderByDesc("want_to_see_count")
          .last("limit " + HIGHLIGHT_FETCH_BATCH))));
    }

    // 4) 仍不够则任意有 cover
    if (presentableCount(candidates) < HIGHLIGHT_LIMIT) {
      candidates.addAll(fetchMovies(excludeExisting(candidates, new QueryWrapper<Movie>()
          .isNotNull("cover")
          .ne("cover", "")
          .orderByDesc("want_to_see_count")
          .last("limit " + HIGHLIGHT_FETCH_BATCH))));
    }

    List<Movie> picked = candidates.stream()
        .filter(this::isPresentable)
        .filter(distinctById())
        .limit(HIGHLIGHT_LIMIT)
        .collect(Collectors.toList());

    // 批量查评分平均
    Map<Integer, Double> ratingMap = loadRatingMap(
        picked.stream().map(Movie::getId).collect(Collectors.toList())
    );

    List<HomeHighlightItem> data = picked.stream()
        .map(m -> toItem(m, ratingMap.get(m.getId())))
        .collect(Collectors.toList());

    return RestBean.success(data, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  private List<Movie> fetchMovies(QueryWrapper<Movie> query) {
    return movieMapper.selectList(query);
  }

  /** 给查询补上「排除已入选 id」条件，避免兜底阶段重复取到同一部电影 */
  private QueryWrapper<Movie> excludeExisting(List<Movie> existing, QueryWrapper<Movie> query) {
    List<Integer> ids = existing.stream().map(Movie::getId).collect(Collectors.toList());
    if (!ids.isEmpty()) {
      query.notIn("id", ids);
    }
    return query;
  }

  /** 按 id 去重的过滤器（保留首次出现，维持原排序） */
  private java.util.function.Predicate<Movie> distinctById() {
    java.util.Set<Integer> seen = new java.util.HashSet<>();
    return m -> m != null && m.getId() != null && seen.add(m.getId());
  }

  private long presentableCount(List<Movie> movies) {
    return movies.stream().filter(this::isPresentable).count();
  }

  /** 过滤纯数字/过短等测试片名，且必须有可用标题 */
  private boolean isPresentable(Movie movie) {
    if (movie == null) return false;
    String cover = movie.getCover();
    if (cover == null || cover.trim().isEmpty()) return false;
    String name = movie.getName() != null ? movie.getName().trim() : "";
    String original = movie.getOriginalName() != null ? movie.getOriginalName().trim() : "";
    if (name.isEmpty() && original.isEmpty()) return false;
    if (isInvalidTitle(name) && isInvalidTitle(original)) return false;
    return true;
  }

  private boolean isInvalidTitle(String title) {
    if (title == null || title.isEmpty()) return true;
    return title.matches("^\\d+$") || title.length() <= 2;
  }

  private HomeHighlightItem toItem(Movie m, Double rating) {
    HomeHighlightItem item = new HomeHighlightItem();
    item.setId(m.getId());
    item.setName(m.getName());
    item.setOriginalName(m.getOriginalName());
    item.setCover(m.getCover());
    item.setDuration(m.getTime());
    item.setRating(rating);
    String start = m.getStartDate();
    if (start != null && start.length() >= 4) {
      item.setYear(start.substring(0, 4));
    }
    return item;
  }

  private Map<Integer, Double> loadRatingMap(List<Integer> movieIds) {
    Map<Integer, Double> result = new HashMap<>();
    if (movieIds == null || movieIds.isEmpty()) return result;
    List<MovieRate> rates = movieRateMapper.selectList(new QueryWrapper<MovieRate>()
        .in("movie_id", movieIds));
    Map<Integer, List<MovieRate>> grouped = rates.stream()
        .collect(Collectors.groupingBy(MovieRate::getMovieId));
    grouped.forEach((id, list) -> {
      double avg = list.stream()
          .mapToDouble(MovieRate::getRate)
          .average()
          .orElse(0d);
      result.put(id, Math.round(avg * 10d) / 10d);
    });
    return result;
  }
}
