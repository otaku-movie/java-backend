package com.example.backend.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.service.FavoriteCinemaService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端「收藏影院」接口。收藏需登录。
 */
@RestController
public class AppFavoriteCinemaController {

  @Autowired
  private FavoriteCinemaService favoriteCinemaService;

  @Data
  static class FavoriteToggleQuery {
    @NotNull(message = "cinemaId required")
    private Integer cinemaId;
  }

  /** 收藏 / 取消收藏（toggle）。返回 toggle 后状态：true=已收藏。 */
  @SaCheckLogin
  @PostMapping(ApiPaths.App.Cinema.FAVORITE_TOGGLE)
  public RestBean<Boolean> toggle(@RequestBody FavoriteToggleQuery query) {
    if (query == null || query.getCinemaId() == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    boolean favorited = favoriteCinemaService.toggle(query.getCinemaId());
    return RestBean.success(favorited, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  /** 当前登录用户收藏的影院 id 列表。 */
  @SaCheckLogin
  @GetMapping(ApiPaths.App.Cinema.FAVORITE_IDS)
  public RestBean<List<Integer>> ids() {
    return RestBean.success(favoriteCinemaService.favoriteCinemaIds(),
        MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
}
