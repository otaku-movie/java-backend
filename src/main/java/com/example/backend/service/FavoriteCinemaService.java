package com.example.backend.service;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.UserFavoriteCinema;
import com.example.backend.mapper.UserFavoriteCinemaMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * C 端「收藏影院」业务。收藏以 cinema_id 为准。
 */
@Service
public class FavoriteCinemaService {

  @Autowired
  private UserFavoriteCinemaMapper favoriteCinemaMapper;

  /**
   * 收藏 / 取消收藏（toggle）。返回 toggle 后的状态：true=已收藏，false=已取消。
   */
  @Transactional
  public boolean toggle(Integer cinemaId) {
    int userId = StpUtil.getLoginIdAsInt();
    UserFavoriteCinema existing = favoriteCinemaMapper.selectOne(
        new LambdaQueryWrapper<UserFavoriteCinema>()
            .eq(UserFavoriteCinema::getUserId, userId)
            .eq(UserFavoriteCinema::getCinemaId, cinemaId)
            .last("LIMIT 1"));

    if (existing != null) {
      favoriteCinemaMapper.deleteById(existing.getId());
      return false;
    }

    UserFavoriteCinema row = new UserFavoriteCinema();
    row.setUserId(userId);
    row.setCinemaId(cinemaId);
    favoriteCinemaMapper.insert(row);
    return true;
  }

  /** 当前登录用户收藏的影院 id 列表。 */
  public List<Integer> favoriteCinemaIds() {
    if (!StpUtil.isLogin()) return Collections.emptyList();
    return favoriteCinemaIds(StpUtil.getLoginIdAsInt());
  }

  /** 指定用户收藏的影院 id 列表。 */
  public List<Integer> favoriteCinemaIds(Integer userId) {
    if (userId == null) return Collections.emptyList();
    return favoriteCinemaMapper
        .selectList(new LambdaQueryWrapper<UserFavoriteCinema>()
            .eq(UserFavoriteCinema::getUserId, userId))
        .stream()
        .map(UserFavoriteCinema::getCinemaId)
        .collect(Collectors.toList());
  }

  public Set<Integer> favoriteCinemaIdSet(Integer userId) {
    return favoriteCinemaIds(userId).stream().collect(Collectors.toSet());
  }

  /** 当前请求是否登录，登录则返回 userId，否则 null。 */
  public Integer currentUserIdOrNull() {
    return StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
  }
}
