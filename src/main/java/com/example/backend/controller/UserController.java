package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.UserListQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.response.LoginResponse;
import com.example.backend.utils.Utils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class UserLoginQuery {
  @NotNull
  String email;
  @NotNull
  String password;
}

@RestController
public class UserController {
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

      return RestBean.success( loginResponse, "登录成功");
    } else  {
      return RestBean.error( 0, "用户不存在");
    }
  }

  @SaCheckLogin
  @GetMapping("/api/user/detail")
  public RestBean<Object> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);
    queryWrapper.select("id", "cover", "name", "email", "create_time", "update_time");
    User result = userMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
}
