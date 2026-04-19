package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Brand;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserCinema;
import com.example.backend.entity.UserRole;
import com.example.backend.enumerate.DataScope;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.BrandMapper;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.UserCinemaMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.query.UserListQuery;
import com.example.backend.query.UserRoleConfigQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.response.AdminLoginResponse;
import com.example.backend.response.AdminUserDetailResponse;
import com.example.backend.response.UserEffectiveButtonResponse;
import com.example.backend.response.UserListResponse;
import com.example.backend.service.UserRoleService;
import com.example.backend.utils.MessageUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
class UserLoginQuery {
  @NotNull
  String email;
  @NotEmpty
  String password;
}

@RestController
public class AdminUserController {
  @Autowired
  private UserMapper userMapper;

  @Autowired
  private CinemaMapper cinemaMapper;

  @Autowired
  private BrandMapper brandMapper;

  @Autowired
  private UserCinemaMapper userCinemaMapper;

  @Autowired
  UserRoleMapper userRoleMapper;

  @Autowired
  UserRoleService userRoleService;

  @Resource
  RedisTemplate redisTemplate;

  /** 管理后台登录（返回数据范围，与 C 端 /api/user/login 区分） */
  @PostMapping(ApiPaths.Admin.User.LOGIN)
  public RestBean<AdminLoginResponse> login(@RequestBody @Validated UserLoginQuery query) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", query.getEmail());
    queryWrapper.eq("password", SaSecureUtil.md5(query.getPassword()));
    queryWrapper.select(
        "id", "cover", "name", "email", "create_time", "data_scope", "brand_id");
    User result = userMapper.selectOne(queryWrapper);
    if (result == null) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Common.User.NOT_FOUND));
    }
    String scope = DataScope.normalize(result.getDataScope());
    if (DataScope.CHAIN.getCode().equals(scope)) {
      if (result.getBrandId() == null) {
        return RestBean.error(
            ResponseCode.PARAMETER_ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CHAIN_BRAND));
      }
      List<Integer> chainCinemaIds =
          resolveCinemaIdsForLogin(scope, result.getBrandId(), result.getId());
      if (chainCinemaIds.isEmpty()) {
        return RestBean.error(
            ResponseCode.PARAMETER_ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CHAIN_NO_CINEMA));
      }
    }
    if (DataScope.CINEMA.getCode().equals(scope)) {
      List<Integer> cinemaIds =
          resolveCinemaIdsForLogin(scope, result.getBrandId(), result.getId());
      if (cinemaIds == null || cinemaIds.isEmpty()) {
        return RestBean.error(
            ResponseCode.PARAMETER_ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CINEMA_IDS));
      }
    }
    StpUtil.login(result.getId());
    AdminLoginResponse resp = new AdminLoginResponse();
    resp.setId(result.getId());
    resp.setName(result.getName());
    resp.setEmail(result.getEmail());
    resp.setCreateTime(result.getCreateTime());
    resp.setCover(result.getCover());
    resp.setToken(StpUtil.getTokenValue());
    resp.setDataScope(scope);
    resp.setBrandId(DataScope.CHAIN.getCode().equals(scope) ? result.getBrandId() : null);
    resp.setCinemaIds(resolveCinemaIdsForLogin(scope, result.getBrandId(), result.getId()));
    return RestBean.success(
        resp, MessageUtils.getMessage(MessageKeys.Common.User.LOGIN_SUCCESS));
  }

  private AdminUserDetailResponse buildAdminUserDetailResponse(User u) {
    String scope = DataScope.normalize(u.getDataScope());
    AdminUserDetailResponse d = new AdminUserDetailResponse();
    d.setId(u.getId());
    d.setCover(u.getCover());
    d.setName(u.getName());
    d.setEmail(u.getEmail());
    d.setCreateTime(u.getCreateTime());
    d.setDataScope(scope);
    d.setBrandId(DataScope.CHAIN.getCode().equals(scope) ? u.getBrandId() : null);
    d.setCinemaIds(resolveCinemaIdsForLogin(scope, u.getBrandId(), u.getId()));
    enrichDetailDisplayFields(d, scope);
    return d;
  }

  private void enrichDetailDisplayFields(AdminUserDetailResponse d, String scope) {
    if (DataScope.CHAIN.getCode().equals(scope) && d.getBrandId() != null) {
      Brand b = brandMapper.selectById(d.getBrandId());
      if (b != null) {
        d.setBrandName(b.getName());
      }
    }
    if (DataScope.CINEMA.getCode().equals(scope)
        && d.getCinemaIds() != null
        && !d.getCinemaIds().isEmpty()) {
      List<Cinema> rows =
          cinemaMapper.selectList(
              new LambdaQueryWrapper<Cinema>().in(Cinema::getId, d.getCinemaIds()));
      Map<Integer, String> idToName = new HashMap<>();
      for (Cinema c : rows) {
        idToName.put(
            c.getId(),
            c.getName() != null && !c.getName().isEmpty() ? c.getName() : ("#" + c.getId()));
      }
      List<String> names = new ArrayList<>();
      for (Integer id : d.getCinemaIds()) {
        names.add(idToName.getOrDefault(id, "#" + id));
      }
      d.setCinemaNames(names);
    }
  }

  private List<Integer> resolveCinemaIdsForLogin(String scope, Integer brandId, int userId) {
    if (DataScope.PLATFORM.getCode().equals(scope)) {
      return Collections.emptyList();
    }
    if (DataScope.CHAIN.getCode().equals(scope)) {
      if (brandId == null) {
        return Collections.emptyList();
      }
      return cinemaMapper
          .selectList(new LambdaQueryWrapper<Cinema>().eq(Cinema::getBrandId, brandId))
          .stream()
          .map(Cinema::getId)
          .collect(Collectors.toList());
    }
    if (DataScope.CINEMA.getCode().equals(scope)) {
      return userCinemaMapper
          .selectList(new LambdaQueryWrapper<UserCinema>().eq(UserCinema::getUserId, userId))
          .stream()
          .map(UserCinema::getCinemaId)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * 院线级操作者是否允许编辑目标用户（不校验请求体中的 dataScope，仅做归属判断）。
   */
  private boolean canChainOperatorEditTargetUser(User operator, User target, String targetScope) {
    if (operator.getBrandId() == null) {
      return false;
    }
    if (DataScope.PLATFORM.getCode().equals(targetScope)) {
      return false;
    }
    if (DataScope.CHAIN.getCode().equals(targetScope)) {
      return Objects.equals(operator.getBrandId(), target.getBrandId());
    }
    if (DataScope.CINEMA.getCode().equals(targetScope)) {
      List<Integer> ids =
          resolveCinemaIdsForLogin(targetScope, target.getBrandId(), target.getId());
      if (ids.isEmpty()) {
        return false;
      }
      for (Integer cid : ids) {
        Cinema c = cid != null ? cinemaMapper.selectById(cid) : null;
        if (c == null || !Objects.equals(c.getBrandId(), operator.getBrandId())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @PostMapping(ApiPaths.Admin.User.LIST)
  public RestBean<List<UserListResponse>> list(@RequestBody @Validated  UserListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<UserListResponse> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<UserListResponse> list = userMapper.userList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @SaCheckLogin
  @CheckPermission(code = "user.configRole")
  @Transactional
  @PostMapping(ApiPaths.Admin.User.CONFIG_ROLE)
  public RestBean<List<Role>> role (@RequestBody @Validated  UserRoleConfigQuery query) {
    userRoleMapper.deleteRole(query.getId());
    userRoleService.saveBatch(
      query.getRoleId().stream().map(item -> {
        UserRole userRole = new UserRole();
        userRole.setUserId(query.getId());
        userRole.setRoleId(item);

        return userRole;
      }).collect(Collectors.toList())
    );


    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "user.save")
  @GetMapping(ApiPaths.Admin.User.DETAIL)
  public RestBean<AdminUserDetailResponse> userDetail(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(
          ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Admin.User.PARAMETER_ERROR));
    }
    User u = userMapper.selectById(id);
    if (u == null) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NOT_FOUND));
    }
    return RestBean.success(
        buildAdminUserDetailResponse(u),
        MessageUtils.getMessage(MessageKeys.Admin.User.GET_SUCCESS));
  }

  /** 当前登录用户详情（数据范围等），无需 user.save 权限，供顶栏与前端会话同步 */
  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.User.CURRENT)
  public RestBean<AdminUserDetailResponse> currentUser() {
    int loginId = StpUtil.getLoginIdAsInt();
    User u = userMapper.selectById(loginId);
    if (u == null) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NOT_FOUND));
    }
    return RestBean.success(
        buildAdminUserDetailResponse(u),
        MessageUtils.getMessage(MessageKeys.Admin.User.GET_SUCCESS));
  }

  @SaCheckLogin
  @GetMapping(ApiPaths.Admin.User.ROLE)
  public RestBean<List<Role>> role (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    List<Role> result = userMapper.userRole(id);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.User.GET_SUCCESS));
  }

  /**
   * 权限预览：合并该用户全部角色下的按钮权限（只读）。与「配置角色」分离，前端可单独按钮调用。
   */
  @SaCheckLogin
  @CheckPermission(code = "user.configRole")
  @GetMapping(ApiPaths.Admin.User.PERMISSION_PREVIEW)
  public RestBean<List<UserEffectiveButtonResponse>> permissionPreview(
      @RequestParam Integer userId) {
    if (userId == null) {
      return RestBean.error(
          ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    List<UserEffectiveButtonResponse> rows = userMapper.userEffectiveButtons(userId);
    return RestBean.success(
        rows, MessageUtils.getMessage(MessageKeys.Admin.User.GET_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "user.remove")
  @Transactional
  @DeleteMapping(ApiPaths.Admin.User.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    Integer userId = StpUtil.getLoginIdAsInt();

    userMapper.deleteById(id);
    QueryWrapper wrapper = new QueryWrapper();

    wrapper.eq("user_id", id);
    userRoleService.remove(wrapper);

    if (id.equals(userId)) {
      StpUtil.logout();
    }

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "user.save")
  @Transactional
  @PostMapping(ApiPaths.Admin.User.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated UserSaveQuery query) {
    int operatorId = StpUtil.getLoginIdAsInt();
    User operator = userMapper.selectById(operatorId);
    if (operator == null) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NOT_FOUND));
    }
    String operatorScope = DataScope.normalize(operator.getDataScope());
    boolean operatorPlatform = DataScope.PLATFORM.getCode().equals(operatorScope);

    String scope = DataScope.normalize(query.getDataScope());
    Integer effectiveBrandId = query.getBrandId();
    List<Integer> effectiveCinemaIds = query.getCinemaIds();

    if (!operatorPlatform) {
      if (query.getId() == null) {
        return RestBean.error(
            ResponseCode.NOT_PERMISSION.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_ONLY_PLATFORM_CAN_CREATE_USER));
      }
      User target = userMapper.selectById(query.getId());
      if (target == null) {
        return RestBean.error(
            ResponseCode.ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NOT_FOUND));
      }
      String targetScope = DataScope.normalize(target.getDataScope());
      if (DataScope.CINEMA.getCode().equals(operatorScope)) {
        if (!DataScope.CINEMA.getCode().equals(targetScope)) {
          return RestBean.error(
              ResponseCode.NOT_PERMISSION.getCode(),
              MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NON_PLATFORM_EDIT_USER_FORBIDDEN));
        }
        List<Integer> operatorCinemaIds =
            resolveCinemaIdsForLogin(operatorScope, operator.getBrandId(), operatorId);
        List<Integer> targetCinemaIds =
            resolveCinemaIdsForLogin(targetScope, target.getBrandId(), target.getId());
        Set<Integer> opSet = new HashSet<>(operatorCinemaIds);
        if (targetCinemaIds.isEmpty() || !opSet.containsAll(targetCinemaIds)) {
          return RestBean.error(
              ResponseCode.NOT_PERMISSION.getCode(),
              MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NON_PLATFORM_EDIT_USER_FORBIDDEN));
        }
      } else if (DataScope.CHAIN.getCode().equals(operatorScope)) {
        if (!canChainOperatorEditTargetUser(operator, target, targetScope)) {
          return RestBean.error(
              ResponseCode.NOT_PERMISSION.getCode(),
              MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NON_PLATFORM_EDIT_USER_FORBIDDEN));
        }
      } else {
        return RestBean.error(
            ResponseCode.NOT_PERMISSION.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_NON_PLATFORM_EDIT_USER_FORBIDDEN));
      }
      scope = targetScope;
      effectiveBrandId = DataScope.CHAIN.getCode().equals(scope) ? target.getBrandId() : null;
      effectiveCinemaIds =
          new ArrayList<>(resolveCinemaIdsForLogin(scope, target.getBrandId(), target.getId()));
    }

    if (DataScope.CHAIN.getCode().equals(scope) && effectiveBrandId == null) {
      return RestBean.error(
          ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CHAIN_BRAND));
    }
    if (DataScope.CHAIN.getCode().equals(scope) && effectiveBrandId != null) {
      long brandCinemaCount =
          cinemaMapper.selectCount(
              new LambdaQueryWrapper<Cinema>().eq(Cinema::getBrandId, effectiveBrandId));
      if (brandCinemaCount == 0) {
        return RestBean.error(
            ResponseCode.PARAMETER_ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CHAIN_NO_CINEMA));
      }
    }
    if (DataScope.CINEMA.getCode().equals(scope)
        && (effectiveCinemaIds == null || effectiveCinemaIds.isEmpty())) {
      return RestBean.error(
          ResponseCode.PARAMETER_ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Error.ADMIN_USER_DATA_SCOPE_CINEMA_IDS));
    }

    User user = new User();
    user.setCover(query.getCover());
    user.setName(query.getName());
    user.setEmail(query.getEmail());
    user.setDataScope(scope);
    if (DataScope.CHAIN.getCode().equals(scope)) {
      user.setBrandId(effectiveBrandId);
    } else {
      user.setBrandId(null);
    }

    if (query.getId() == null) {
      if (query.getPassword() == null) {
        return RestBean.error(
            ResponseCode.PARAMETER_ERROR.getCode(),
            MessageUtils.getMessage(MessageKeys.Validator.SaveUser.PASSWORD_REQUIRED));
      }
      user.setPassword(SaSecureUtil.md5(query.getPassword()));
    } else {
      if (query.getPassword() != null) {
        user.setPassword(SaSecureUtil.md5(query.getPassword()));
      }
    }

    String key = RedisType.verifyCode.getCode() + ':' + query.getToken();
    Object code = redisTemplate.opsForValue().get(key);
    if (code == null) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Validator.SaveUser.CODE_EXPIRED));
    }
    if (!String.valueOf(code).equals(query.getCode())) {
      return RestBean.error(
          ResponseCode.ERROR.getCode(),
          MessageUtils.getMessage(MessageKeys.Validator.SaveUser.CODE_ERROR));
    }

    if (query.getId() == null) {
      QueryWrapper<User> emailQw = new QueryWrapper<>();
      emailQw.eq("email", query.getEmail());
      if (userMapper.selectCount(emailQw) > 0) {
        return RestBean.error(
            ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.User.EMAIL_REPEAT));
      }
      userMapper.insert(user);
      syncUserCinemas(user.getId(), scope, effectiveCinemaIds);
      return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.User.SAVE_SUCCESS));
    }

    QueryWrapper<User> dupQw = new QueryWrapper<>();
    dupQw.eq("email", query.getEmail()).ne("id", query.getId());
    if (userMapper.selectCount(dupQw) > 0) {
      return RestBean.error(
          ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.User.EMAIL_REPEAT));
    }
    LambdaUpdateWrapper<User> uw = new LambdaUpdateWrapper<>();
    uw.eq(User::getId, query.getId())
        .set(User::getCover, query.getCover())
        .set(User::getName, query.getName())
        .set(User::getEmail, query.getEmail())
        .set(User::getDataScope, scope)
        .set(
            User::getBrandId,
            DataScope.CHAIN.getCode().equals(scope) ? effectiveBrandId : null);
    if (query.getPassword() != null) {
      uw.set(User::getPassword, SaSecureUtil.md5(query.getPassword()));
    }
    userMapper.update(null, uw);
    syncUserCinemas(query.getId(), scope, effectiveCinemaIds);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.User.SAVE_SUCCESS));
  }

  private void syncUserCinemas(Integer userId, String scope, List<Integer> cinemaIds) {
    userCinemaMapper.delete(
        new LambdaQueryWrapper<UserCinema>().eq(UserCinema::getUserId, userId));
    if (!DataScope.CINEMA.getCode().equals(scope) || cinemaIds == null) {
      return;
    }
    List<UserCinema> batch = new ArrayList<>();
    for (Integer cid : cinemaIds) {
      if (cid == null) {
        continue;
      }
      UserCinema uc = new UserCinema();
      uc.setUserId(userId);
      uc.setCinemaId(cid);
      batch.add(uc);
    }
    for (UserCinema uc : batch) {
      userCinemaMapper.insert(uc);
    }
  }
}
