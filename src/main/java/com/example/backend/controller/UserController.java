package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import com.example.backend.query.auth.TwitterLoginQuery;
import com.example.backend.query.PaginationQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.query.auth.RefreshTokenQuery;
import com.example.backend.response.AppLoginResponse;
import com.example.backend.response.order.OrderListResponse;
import com.example.backend.service.OAuthIdTokenVerifier;
import com.example.backend.service.RefreshTokenService;
import com.example.backend.service.XOAuthService;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.PasswordUtil;
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
  /**
   * 是否设置过本地密码。前端用来判断「最后一个登录方式」保护：
   * hasPassword=false 且仅剩 1 个 oauthBinding 时，解绑按钮应禁用并提示。
   */
  boolean hasPassword;
  /** 已绑定的第三方身份列表（已脱敏：不返回 subject / rawProfile）。 */
  java.util.List<OAuthBindingItem> oauthBindings;
}

@Data
class OAuthBindingItem {
  /** google / apple / x */
  String provider;
  /** 在该 provider 下的展示名（最近一次同步），不一定等于本地 user.name。 */
  String name;
  /** 在该 provider 下的头像 URL，可能为 null（X 必为 null 或来自 X CDN）。 */
  String picture;
  /** 在该 provider 下的邮箱（X 无此字段）。 */
  String email;
  /** 最近一次通过该 provider 登录的时间。 */
  @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  java.util.Date lastLoginAt;
  /** 首次绑定时间。 */
  @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  java.util.Date createTime;
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
  private XOAuthService xOAuthService;

  @Autowired
  private UserOAuthBindingMapper userOAuthBindingMapper;

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping(ApiPaths.Common.User.LOGIN)
  public RestBean<AppLoginResponse> login(@RequestBody @Validated UserLoginQuery query) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", query.getEmail());
    queryWrapper.eq("deleted", 0);
    // 取出 password 用于校验（BCrypt 无法用等值查询匹配）；User.password 有 @JsonIgnore 不会泄露
    queryWrapper.select("id", "cover", "name", "email", "create_time", "password");

    User result = userMapper.selectOne(queryWrapper);

    if (result != null && PasswordUtil.matches(query.getPassword(), result.getPassword())) {
      // 老的无盐 md5 密码：校验通过后自动升级为 BCrypt
      if (PasswordUtil.needsUpgrade(result.getPassword())) {
        userMapper.update(null, new UpdateWrapper<User>()
            .set("password", PasswordUtil.encode(query.getPassword()))
            .eq("id", result.getId()));
      }
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
    OAuthIdTokenVerifier.OAuthProfile profile = oAuthIdTokenVerifier.verifyApple(query.getIdToken(), query.getNonce());
    // Apple 只在首次登录返回 fullName，客户端会原样回传给后端，这里补到 profile 上
    // 供后续 findOrCreateOAuthUser 创建用户/绑定时使用。
    applyOAuthFallbackName(profile, query.getFirstName(), query.getLastName());
    User user = findOrCreateOAuthUser(profile);
    return RestBean.success(
        refreshTokenService.issueLoginResponse(user, query.getDeviceId()),
        messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS)
    );
  }

  /**
   * X (Twitter) 登录 - OAuth 2.0 PKCE。
   *
   * <p>客户端走 PKCE 拿到 access_token 后回传，后端调 X /2/users/me 验真：
   * <ul>
   *   <li>HTTP 200 → 取 id/name/username/profile_image_url</li>
   *   <li>HTTP 401/403 → token 过期或撤销，返回 4xx 让客户端重新走登录</li>
   * </ul>
   * </p>
   */
  @PostMapping(ApiPaths.Common.User.TWITTER_LOGIN)
  public RestBean<AppLoginResponse> twitterLogin(@RequestBody @Validated TwitterLoginQuery query) {
    try {
      OAuthIdTokenVerifier.OAuthProfile profile = xOAuthService.fetchProfile(query.getAccessToken());
      User user = findOrCreateOAuthUser(profile);
      return RestBean.success(
          refreshTokenService.issueLoginResponse(user, query.getDeviceId()),
          messageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS)
      );
    } catch (IllegalArgumentException e) {
      return RestBean.error(ResponseCode.ERROR.getCode(), "x login failed: " + e.getMessage());
    }
  }

  /**
   * 若 idToken 中没有 name（典型场景：Apple 二次及以后登录），用客户端补传的姓名兜底。
   * idToken 已包含 name 时优先采用 token 中的值（更可信）。
   */
  private void applyOAuthFallbackName(OAuthIdTokenVerifier.OAuthProfile profile, String firstName, String lastName) {
    if (profile == null) return;
    if (profile.getName() != null && !profile.getName().isBlank()) return;
    StringBuilder sb = new StringBuilder();
    if (firstName != null && !firstName.isBlank()) sb.append(firstName.trim());
    if (lastName != null && !lastName.isBlank()) {
      if (sb.length() > 0) sb.append(' ');
      sb.append(lastName.trim());
    }
    if (sb.length() > 0) profile.setName(sb.toString());
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
    user.setPassword(PasswordUtil.encode(query.getPassword()));


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
      // 注册成功后直接按 email 取回刚插入的用户（密码已是 BCrypt，无法用等值匹配）
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
    int userId = StpUtil.getLoginIdAsInt();
    // 注意：这里必须把 password 也查出来用于判断 hasPassword，再 BeanUtils 复制属性时
    // User.password 有 @JsonIgnore，不会泄露给前端。
    User result = userMapper.selectOne(new QueryWrapper<User>()
        .eq("id", userId)
        .eq("deleted", 0)
        .select("id", "cover", "name", "email", "create_time", "password"));
    if (result == null) {
      // token 指向已删用户或跨库旧会话时，避免 BeanUtils 抛出 "Source must not be null"。
      StpUtil.logout();
      return RestBean.error(ResponseCode.ERROR.getCode(), messageUtils.getMessage(MessageKeys.Common.User.NOT_FOUND));
    }
    UserDetail userDetail = new UserDetail();
    BeanUtils.copyProperties(result, userDetail);
    userDetail.setOrderCount(userMapper.countDistinctMovieOrders(userId));
    userDetail.setHasPassword(result.getPassword() != null && !result.getPassword().isBlank());

    List<UserOAuthBinding> bindings = userOAuthBindingMapper.selectList(
        new QueryWrapper<UserOAuthBinding>()
            .eq("user_id", userId)
            .eq("deleted", 0)
            .orderByDesc("last_login_at"));
    userDetail.setOauthBindings(bindings.stream().map(b -> {
      OAuthBindingItem item = new OAuthBindingItem();
      item.setProvider(b.getProvider());
      item.setName(b.getName());
      item.setPicture(b.getPicture());
      item.setEmail(b.getEmail());
      item.setLastLoginAt(b.getLastLoginAt());
      item.setCreateTime(b.getCreateTime());
      return item;
    }).collect(java.util.stream.Collectors.toList()));

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

    // 注销只做软删除：仅置 deleted=1，保留 email/name/cover/password 原值。
    // 原因：1) 这些字段的覆盖只是脱敏，不是唯一约束需要（idx_users_email 是
    //          partial 非唯一索引 WHERE deleted=0，软删后不会与新注册冲突）；
    //       2) 硬覆盖不可逆，一旦误操作原始资料无法找回。保留原值后，万一
    //          管理员误注销，只需把 users + user_oauth_binding 两处 deleted 改回 0 即可复原。
    // 登录安全性不受影响：下面同时软删 user_oauth_binding，第三方登录按
    // (provider, subject, deleted=0) 匹配，注销账号不会再被复用。
    userMapper.update(
        null,
        new UpdateWrapper<User>()
            .set("deleted", 1)
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
      // X 等 provider 不返回邮箱，email 允许为 null（见 V53 迁移去掉 NOT NULL）。
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
