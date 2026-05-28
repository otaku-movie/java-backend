package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.entity.UserOAuthBinding;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserOAuthBindingMapper;
import com.example.backend.query.auth.OAuthLoginQuery;
import com.example.backend.query.PaginationQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.query.auth.RefreshTokenQuery;
import com.example.backend.response.AppLoginResponse;
import com.example.backend.response.order.OrderListResponse;
import com.example.backend.service.OAuthIdTokenVerifier;
import com.example.backend.service.RefreshTokenService;
import com.example.backend.utils.MessageUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Data
class UserLoginQuery {
  @NotEmpty(message = "{validator.login.email.required}")
  @Email(message = "{validator.login.email.notEmail}")
  String email;
  @NotEmpty(message = "{validator.login.password.required}")
  String password;
  String deviceId;
}

@Data
class UpdateUserInfoQuery {
  @NotNull
  Integer id;

  @NotEmpty(message = "{validator.login.email.required}")
  @Email(message = "{validator.login.email.notEmail}")
  String email;

  @NotEmpty
  String username;
  String cover;
}

@Data
class UserDetail extends User{
  int orderCount;
}

@RestController
public class UserController {
  @Autowired
  private MessageUtils messageUtils;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private MovieOrderMapper movieOrderMapper;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private OAuthIdTokenVerifier oAuthIdTokenVerifier;

  @Autowired
  private UserOAuthBindingMapper userOAuthBindingMapper;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping(ApiPaths.Common.User.LOGIN)
  public RestBean<AppLoginResponse> login(@RequestBody @Validated UserLoginQuery query) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", query.getEmail());

    queryWrapper.eq("password", SaSecureUtil.md5(query.getPassword()));
    queryWrapper.eq("deleted", 0);
    queryWrapper.select("id", "cover", "name", "email", "create_time");

    User result = userMapper.selectOne(queryWrapper);

    if (result != null) {
      StpUtil.login(result.getId());
      AppLoginResponse loginResponse = new AppLoginResponse();
      loginResponse.setId(result.getId());
      loginResponse.setName(result.getName());
      loginResponse.setEmail(result.getEmail());
      loginResponse.setCreateTime(result.getCreateTime());
      loginResponse.setCover(result.getCover());
      loginResponse = refreshTokenService.issueLoginResponse(result, safeDeviceId(query.getDeviceId()));

      return RestBean.success( loginResponse, messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS));
    } else  {
      return RestBean.error(ResponseCode.ERROR.getCode(), messageUtils.getMessage(MessageKeys.Common.User.NOT_FOUND));
    }
  }

  @PostMapping(ApiPaths.Common.User.REFRESH)
  public RestBean<AppLoginResponse> refresh(@RequestBody @Validated RefreshTokenQuery query) {
    try {
      return RestBean.success(
          refreshTokenService.refresh(query.getRefreshToken(), query.getDeviceId()),
          messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS)
      );
    } catch (IllegalArgumentException e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), "refresh token invalid");
    }
  }

  @PostMapping(ApiPaths.Common.User.GOOGLE_LOGIN)
  public RestBean<AppLoginResponse> googleLogin(@RequestBody @Validated OAuthLoginQuery query) {
    OAuthIdTokenVerifier.OAuthProfile profile = oAuthIdTokenVerifier.verifyGoogle(query.getIdToken());
    User user = findOrCreateOAuthUser(profile);
    return RestBean.success(
        refreshTokenService.issueLoginResponse(user, query.getDeviceId()),
        messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS)
    );
  }

  @PostMapping(ApiPaths.Common.User.APPLE_LOGIN)
  public RestBean<AppLoginResponse> appleLogin(@RequestBody @Validated OAuthLoginQuery query) {
    OAuthIdTokenVerifier.OAuthProfile profile = oAuthIdTokenVerifier.verifyApple(query.getIdToken());
    User user = findOrCreateOAuthUser(profile);
    return RestBean.success(
        refreshTokenService.issueLoginResponse(user, query.getDeviceId()),
        messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS)
    );
  }
  @PostMapping(ApiPaths.Common.User.UPDATE_INFO)
  public RestBean<Null> updateUserInfo(@RequestBody @Validated UpdateUserInfoQuery query){
    User modal = new User();

    modal.setCover(query.getCover());
    modal.setName(query.getUsername());
    modal.setId(query.getId());

    userMapper.updateById(modal);

    return  RestBean.success(null, messageUtils.getMessage(MessageKeys.Success.SAVE));
  }

  @PostMapping(ApiPaths.Common.User.REGISTER)
  public RestBean<AppLoginResponse> save(@RequestBody @Validated UserSaveQuery query) {
    User user = new User();

    user.setCover(query.getCover());
    user.setName(query.getName());
    user.setEmail(query.getEmail());
    user.setPassword(SaSecureUtil.md5(query.getPassword()));


    // 验证邮箱是否有效
    String key = RedisType.verifyCode.getCode() + ':' + query.getToken();

    Object code = redisTemplate.opsForValue().get(key);

    if (code == null) {
      return RestBean.error(
        ResponseCode.ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Validator.SaveUser.CODE_EXPIRED)
      );
    } else if (code != null && code != query.getCode()) {
      return RestBean.error(
        ResponseCode.ERROR.getCode(),
        MessageUtils.getMessage(MessageKeys.Validator.SaveUser.CODE_ERROR)
      );
    }
    // 验证邮箱是否已经注册过
    QueryWrapper<User> wrapper = new QueryWrapper<>();
    wrapper.eq("email", query.getEmail());
    wrapper.eq("deleted", 0);
    Long count = userMapper.selectCount(wrapper);

    if (count == 0) {
      userMapper.insert(user);
      // 登录完成后查询并返回用户信息直接登录
      AppLoginResponse loginResponse = new AppLoginResponse();
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("email", query.getEmail());

      queryWrapper.eq("password", SaSecureUtil.md5(query.getPassword()));
      queryWrapper.eq("deleted", 0);
      queryWrapper.select("id", "cover", "name", "email", "create_time");
      User result = userMapper.selectOne(queryWrapper);

      if (result != null) {
        loginResponse = refreshTokenService.issueLoginResponse(result, safeDeviceId(query.getDeviceId()));

        return RestBean.success(loginResponse, messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS));
      }
    } else {
      return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Error.EMAIL_REPEAT));
    }
    return null;
  }
  @SaCheckLogin
  @GetMapping(ApiPaths.Common.User.DETAIL)
  public RestBean<Object> detail () {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    int userId = StpUtil.getLoginIdAsInt();
    queryWrapper.eq("id", StpUtil.getLoginIdAsInt());
    queryWrapper.eq("deleted", 0);
    queryWrapper.select("id", "cover", "name", "email", "create_time");
    User result = userMapper.selectOne(queryWrapper);
    UserDetail userDetail = new UserDetail();
    BeanUtils.copyProperties(result, userDetail);  // 直接复制属性
    userDetail.setOrderCount(userMapper.countDistinctMovieOrders(userId));
    return RestBean.success(userDetail, messageUtils.getMessage(MessageKeys.Success.GET));
  }
  @SaCheckLogin
  @PostMapping(ApiPaths.Common.User.ORDER_LIST)
  public RestBean<List<OrderListResponse>> orderList(@RequestBody PaginationQuery query) {
    Page<OrderListResponse> page = new Page<>(query.getPage(), query.getPageSize());
    int userId = StpUtil.getLoginIdAsInt();

    IPage<OrderListResponse> list = movieOrderMapper.userOrderList(userId, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @PostMapping(ApiPaths.Common.User.LOGOUT)
  public RestBean<Null> logout(@RequestHeader(value = "refresh-token", required = false) String refreshToken) {
    refreshTokenService.revoke(refreshToken);
    StpUtil.logout(StpUtil.getLoginId());
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Common.User.LOGOUT_SUCCESS));
  }

  @SaCheckLogin
  @PostMapping(ApiPaths.Common.User.DELETE_ACCOUNT)
  public RestBean<Null> deleteAccount() {
    int userId = StpUtil.getLoginIdAsInt();
    String anonymizedEmail = "deleted_" + userId + "_" + System.currentTimeMillis() + "@deleted.local";

    userMapper.update(
        null,
        new UpdateWrapper<User>()
            .set("deleted", 1)
            .set("email", anonymizedEmail)
            .set("name", "Deleted User")
            .set("cover", null)
            .set("password", null)
            .eq("id", userId)
            .eq("deleted", 0)
    );
    userOAuthBindingMapper.update(
        null,
        new UpdateWrapper<UserOAuthBinding>()
            .set("deleted", 1)
            .eq("user_id", userId)
            .eq("deleted", 0)
    );
    refreshTokenService.revokeAllForUser(userId);
    StpUtil.logout(userId);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Common.User.LOGOUT_SUCCESS));
  }

  private String safeDeviceId(String deviceId) {
    return deviceId == null || deviceId.isBlank() ? "legacy" : deviceId;
  }

  /**
   * OAuth 登录登录态查找：优先按 (provider, subject) 命中绑定；否则按 email 关联已有
   * 账号或创建新账号，最后写/更新一条 user_oauth_binding 记录。
   */
  private User findOrCreateOAuthUser(OAuthIdTokenVerifier.OAuthProfile profile) {
    UserOAuthBinding binding = userOAuthBindingMapper.selectOne(
        new QueryWrapper<UserOAuthBinding>()
            .eq("provider", profile.getProvider())
            .eq("subject", profile.getSubject())
            .eq("deleted", 0)
            .last("LIMIT 1"));

    User user = null;
    if (binding != null) {
      user = userMapper.selectById(binding.getUserId());
    }

    // 仅在 provider 已验证邮箱时，才允许凭 email 自动绑定到既有账号，
    // 防止未验证邮箱的 id_token 套登他人账号。
    if (user == null
        && profile.getEmail() != null
        && Boolean.TRUE.equals(profile.getEmailVerified())) {
      user = userMapper.selectOne(new QueryWrapper<User>()
          .eq("email", profile.getEmail())
          .eq("deleted", 0)
          .last("LIMIT 1"));
    }

    if (user == null) {
      user = new User();
      user.setEmail(profile.getEmail());
      user.setName(resolveOAuthDisplayName(profile));
      user.setCover(profile.getPicture());
      userMapper.insert(user);
      user = userMapper.selectById(user.getId());
    }

    upsertOAuthBinding(user.getId(), profile, binding);
    return user;
  }

  /**
   * 兜底用户名：profile.name → email → "{provider} 用户"。Apple 可能两者都缺。
   */
  private String resolveOAuthDisplayName(OAuthIdTokenVerifier.OAuthProfile profile) {
    if (profile.getName() != null && !profile.getName().isBlank()) return profile.getName();
    if (profile.getEmail() != null && !profile.getEmail().isBlank()) return profile.getEmail();
    String provider = profile.getProvider() == null ? "oauth" : profile.getProvider();
    return provider + " 用户";
  }

  private void upsertOAuthBinding(
      Integer userId,
      OAuthIdTokenVerifier.OAuthProfile profile,
      UserOAuthBinding existing) {
    java.util.Date now = new java.util.Date();
    if (existing == null) {
      UserOAuthBinding row = new UserOAuthBinding();
      row.setUserId(userId);
      row.setProvider(profile.getProvider());
      row.setSubject(profile.getSubject());
      row.setEmail(profile.getEmail());
      row.setName(profile.getName());
      row.setPicture(profile.getPicture());
      row.setLastLoginAt(now);
      userOAuthBindingMapper.insert(row);
      return;
    }
    UserOAuthBinding update = new UserOAuthBinding();
    update.setId(existing.getId());
    update.setEmail(profile.getEmail());
    update.setName(profile.getName());
    update.setPicture(profile.getPicture());
    update.setLastLoginAt(now);
    userOAuthBindingMapper.updateById(update);
  }
}
