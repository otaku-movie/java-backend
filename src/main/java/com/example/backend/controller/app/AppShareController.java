package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieRate;
import com.example.backend.entity.MovieTag;
import com.example.backend.entity.MovieTagTags;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.mapper.MovieTagMapper;
import com.example.backend.mapper.MovieTagTagsMapper;
import com.example.backend.response.app.MovieShareDetailResponse;
import com.example.backend.utils.MessageUtils;
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
}
