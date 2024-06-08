package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.query.UserListQuery;
import com.example.backend.query.UserRoleConfigQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.response.UserListResponse;
import com.example.backend.service.UserRoleService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
class UserLoginQuery {
  @NotNull
  String email;
  String password;
}

@RestController
public class AdminUserController {
  @Autowired
  private MessageUtils messageUtils;
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
  @SaCheckLogin
  @CheckPermission(code = "user.configRole")
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


    return RestBean.success(null, MessageUtils.getMessage("success.save"));
  }
  @SaCheckLogin
  @GetMapping("/api/admin/user/role")
  public RestBean<List<Role>> role (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    List<Role> result = userMapper.userRole(id);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "user.remove")
  @Transactional
  @DeleteMapping("/api/admin/user/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    Integer userId = StpUtil.getLoginIdAsInt();

    userMapper.deleteById(id);
    QueryWrapper wrapper = new QueryWrapper();

    wrapper.eq("user_id", id);
    userRoleService.remove(wrapper);

    if (id.equals(userId)) {
      StpUtil.logout();
    }

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "user.save")
  @PostMapping("/api/admin/user/save")
  public RestBean<List<Object>> save(@RequestBody @Validated UserSaveQuery query)  {
    User user = new User();

    user.setCover(query.getCover());
    user.setName(query.getName());
    user.setEmail(query.getEmail());

    if (query.getId() == null) {
      if (query.getPassword() == null) {
        return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage("validator.saveUser.password.required"));
      }
      user.setPassword(SaSecureUtil.md5(query.getPassword()));
    } else {
      // 编辑的时候，如果有密码，就修改
      if (query.getPassword() != null) {
        user.setPassword(SaSecureUtil.md5(query.getPassword()));
      }
    }

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("email", query.getEmail());
      List<User> list = userMapper.selectList(wrapper);

      if (list.size() == 0) {
        userMapper.insert(user);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.emailRepeat"));
      }
    } else {


      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("email", query.getEmail());
      wrapper.ne("id", query.getId());

      Long count = userMapper.selectCount(wrapper);


      if (count == 0) {
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());

        user.setId(query.getId());
        userMapper.update(user, updateQueryWrapper);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.emailRepeat"));
      }

    }
  }
}
