package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieOrderMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.query.PaginationQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.query.order.MovieOrderListQuery;
import com.example.backend.response.LoginResponse;
import com.example.backend.response.order.OrderListResponse;
import com.example.backend.utils.MessageUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.backend.enumerate.RedisType;

import java.util.List;


@Data
class UserLoginQuery {
  @NotEmpty(message = "{validator.login.email.required}")
  @Email(message = "{validator.login.email.notEmail}")
  String email;
  @NotEmpty(message = "{validator.login.password.required}")
  String password;
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

  @Resource
  RedisTemplate redisTemplate;

  @PostMapping("/api/user/login")
  public RestBean<LoginResponse> login(@RequestBody @Validated UserLoginQuery query) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", query.getEmail());

    queryWrapper.eq("password", SaSecureUtil.md5(query.getPassword()));
    queryWrapper.select("id", "cover", "name", "email", "create_time");

    LoginResponse loginResponse = new LoginResponse();
    User result = userMapper.selectOne(queryWrapper);

    if (result != null) {
      StpUtil.login(result.getId());
      loginResponse.setId(result.getId());
      loginResponse.setName(result.getName());
      loginResponse.setEmail(result.getEmail());
      loginResponse.setCreateTime(result.getCreateTime());
      loginResponse.setCover(result.getCover());
      loginResponse.setToken(StpUtil.getTokenValue());

      return RestBean.success( loginResponse, messageUtils.getMessage("success.login"));
    } else  {
      return RestBean.error(ResponseCode.ERROR.getCode(), messageUtils.getMessage("error.userNotFound"));
    }
  }
  @PostMapping("/api/user/updateUserInfo")
  public RestBean<Null> updateUserInfo(@RequestBody @Validated UpdateUserInfoQuery query){
    User modal = new User();

    modal.setCover(query.getCover());
    modal.setName(query.getUsername());
    modal.setId(query.getId());

    userMapper.updateById(modal);

    return  RestBean.success(null, messageUtils.getMessage("success.save"));
  }

  @PostMapping("/api/user/register")
  public RestBean<LoginResponse> save(@RequestBody @Validated UserSaveQuery query) {
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
        MessageUtils.getMessage("validator.saveUser.code.expired")
      );
    } else if (code != null && code != query.getCode()) {
      return RestBean.error(
        ResponseCode.ERROR.getCode(),
        MessageUtils.getMessage("validator.saveUser.code.error")
      );
    }
    // 验证邮箱是否已经注册过
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("email", query.getEmail());
    Long count = userMapper.selectCount(wrapper);

    if (count == 0) {
      userMapper.insert(user);
      // 登录完成后查询并返回用户信息直接登录
      LoginResponse loginResponse = new LoginResponse();
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("email", query.getEmail());

      queryWrapper.eq("password", SaSecureUtil.md5(query.getPassword()));
      queryWrapper.select("id", "cover", "name", "email", "create_time");
      User result = userMapper.selectOne(queryWrapper);

      if (result != null) {
        StpUtil.login(result.getId());
        loginResponse.setId(result.getId());
        loginResponse.setName(result.getName());
        loginResponse.setEmail(result.getEmail());
        loginResponse.setCreateTime(result.getCreateTime());
        loginResponse.setCover(result.getCover());
        loginResponse.setToken(StpUtil.getTokenValue());

        return RestBean.success(loginResponse, messageUtils.getMessage("success.login"));
      }
    } else {
      return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.emailRepeat"));
    }
    return null;
  }
  @SaCheckLogin
  @GetMapping("/api/user/detail")
  public RestBean<Object> detail () {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    int userId = StpUtil.getLoginIdAsInt();
    queryWrapper.eq("id", StpUtil.getLoginIdAsInt());
    queryWrapper.select("id", "cover", "name", "email", "create_time");
    User result = userMapper.selectOne(queryWrapper);
    UserDetail userDetail = new UserDetail();
    BeanUtils.copyProperties(result, userDetail);  // 直接复制属性
    userDetail.setOrderCount(userMapper.countDistinctMovieOrders(userId));
    return RestBean.success(userDetail, messageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @PostMapping("/api/user/orderList")
  public RestBean<List<OrderListResponse>> orderList(@RequestBody PaginationQuery query) {
    Page<OrderListResponse> page = new Page<>(query.getPage(), query.getPageSize());
    int userId = StpUtil.getLoginIdAsInt();

    IPage<OrderListResponse> list = movieOrderMapper.userOrderList(userId, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @PostMapping("/api/user/logout")
  public RestBean<Null> logout() {
    StpUtil.logout(StpUtil.getLoginId());
    return RestBean.success(null, MessageUtils.getMessage("success.logout"));
  }
}
