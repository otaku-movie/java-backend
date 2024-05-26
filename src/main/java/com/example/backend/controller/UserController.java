package com.example.backend.controller;

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

  @PostMapping("/api/user/list")
  public RestBean<List<Object>> list(@RequestBody UserListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<User> page = new Page<>(query.getPage() - 1, query.getPageSize());

    if (query.getEmail() != null && query.getEmail() != "") {
      wrapper.eq("email", query.getEmail());
    }
    if (query.getName() != null && query.getName() != "") {
      wrapper.like("username", query.getName());
    }
    if (query.getId() != null) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByAsc("update_time");
    wrapper.select("id", "username", "email", "create_time");

    IPage list = userMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @PostMapping("/api/user/login")
  public RestBean<User> login(@RequestBody @Validated UserLoginQuery query) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", query.getEmail());
    queryWrapper.eq("password", query.getPassword());

    User result = userMapper.selectOne(queryWrapper);

    return RestBean.success( result, "获取成功");
  }


  @GetMapping("/api/user/detail")
  public RestBean<User> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);
    queryWrapper.select("id", "username", "email", "create_time");
    User result = userMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @GetMapping("/api/user/role")
  public RestBean<List<Role>> role (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    List<Role> result = userMapper.userRole(id);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/user/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    userMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/user/save")
  public RestBean<List<Object>> save(@RequestBody @Validated UserSaveQuery query)  {
    User user = new User();

    user.setCover(query.getCover());
    user.setUsername(query.getUsername());
    user.setPassword(query.getPassword());
    user.setEmail(query.getEmail());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("email", query.getEmail());
      List<User> list = userMapper.selectList(wrapper);

      if (list.size() == 0) {
        userMapper.insert(user);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前用户已经存在");
      }
    } else {
      user.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      User old = userMapper.selectById(query.getId());

      if (Objects.equals(old.getEmail(), query.getEmail()) && old.getId() == query.getId()) {
        userMapper.update(user, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getEmail());
        User find = userMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前用户已经存在");
        } else {
          userMapper.update(user, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }

}
