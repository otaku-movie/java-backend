package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.MovieRate;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieRateMapper;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Data
class MovieRateSaveQuery {
  @NotNull(message = "{validator.movieRate.movieId.required}")
  Integer movieId;

  @NotNull(message = "{validator.movieRate.rate.required}")
  @DecimalMin(value = "0.1", message = "{validator.movieRate.rate.min}")
  @DecimalMax(value = "10.0", message = "{validator.movieRate.rate.max}")
  Double rate;
}

@RestController
public class MovieRateController {
  @Autowired
  private MovieRateMapper movieRateMapper;

  /**
   * 对电影评分（每用户每电影仅一条，可覆盖更新）。
   */
  @SaCheckLogin
  @Transactional
  @PostMapping(ApiPaths.Common.Rate.SAVE)
  public RestBean<Object> save(@RequestBody @Validated MovieRateSaveQuery query) {
    Integer userId = Utils.getUserId();

    QueryWrapper<MovieRate> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id", userId);
    wrapper.eq("movie_id", query.getMovieId());

    MovieRate existing = movieRateMapper.selectOne(wrapper);
    if (existing != null) {
      existing.setRate(query.getRate());
      movieRateMapper.updateById(existing);
    } else {
      MovieRate movieRate = new MovieRate();
      movieRate.setMovieId(query.getMovieId());
      movieRate.setUserId(userId);
      movieRate.setRate(query.getRate());
      movieRateMapper.insert(movieRate);
    }

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
}
