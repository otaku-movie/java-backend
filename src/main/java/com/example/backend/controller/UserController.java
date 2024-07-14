package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.User;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.UserMapper;
import com.example.backend.response.LoginResponse;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Data
class UserLoginQuery {
  @NotEmpty(message = "{validator.login.email.required}")
  @Email(message = "{validator.login.email.notEmail}")
  String email;
  @NotEmpty(message = "{validator.login.password.required}")
  String password;
}

@RestController
public class UserController {
  @Autowired
  private MessageUtils messageUtils;

  @Autowired
  private UserMapper userMapper;

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
      loginResponse.setCreate_time(result.getCreateTime());
      loginResponse.setCover(result.getCover());
      loginResponse.setToken(StpUtil.getTokenValue());

      return RestBean.success( loginResponse, messageUtils.getMessage("success.login"));
    } else  {
      return RestBean.error(ResponseCode.ERROR.getCode(), messageUtils.getMessage("error.userNotFound"));
    }
  }

  @SaCheckLogin
  @GetMapping("/api/user/detail")
  public RestBean<Object> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);
    queryWrapper.select("id", "cover", "name", "email", "create_time");
    User result = userMapper.selectOne(queryWrapper);

    return RestBean.success(result, messageUtils.getMessage("success.get"));
  }
}
