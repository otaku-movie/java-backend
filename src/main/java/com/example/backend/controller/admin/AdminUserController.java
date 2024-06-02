package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.query.UserListQuery;
import com.example.backend.query.UserRoleConfigQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.response.UserListResponse;
import com.example.backend.service.UserRoleService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
class UserLoginQuery {
  @NotNull
  String email;
  @NotNull
  String password;
}

@RestController
public class AdminUserController {
  @Autowired
  private UserMapper userMapper;

  @Autowired
  UserRoleMapper userRoleMapper;

  @Autowired
  UserRoleService userRoleService;

  @PostMapping("/api/admin/user/list")
  public RestBean<List<UserListResponse>> list(@RequestBody @Validated  UserListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<UserListResponse> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<UserListResponse> list = userMapper.userList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @Transactional
  @PostMapping("/api/admin/user/configRole")
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


    return RestBean.success(null, "success");
  }

  @GetMapping("/api/admin/user/role")
  public RestBean<List<Role>> role (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    List<Role> result = userMapper.userRole(id);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/admin/user/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    userMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/admin/user/save")
  public RestBean<List<Object>> save(@RequestBody @Validated UserSaveQuery query)  {
    User user = new User();

    user.setCover(query.getCover());
    user.setName(query.getName());
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
