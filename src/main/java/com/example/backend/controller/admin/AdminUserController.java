package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.enumerate.RedisType;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.query.UserListQuery;
import com.example.backend.query.UserRoleConfigQuery;
import com.example.backend.query.UserSaveQuery;
import com.example.backend.response.UserListResponse;
import com.example.backend.service.UserRoleService;
import com.example.backend.utils.MessageUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
  private UserMapper userMapper;

  @Autowired
  UserRoleMapper userRoleMapper;

  @Autowired
  UserRoleService userRoleService;

  @Resource
  RedisTemplate redisTemplate;

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
  @GetMapping(ApiPaths.Admin.User.ROLE)
  public RestBean<List<Role>> role (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    List<Role> result = userMapper.userRole(id);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.User.GET_SUCCESS));
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
  @PostMapping(ApiPaths.Admin.User.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated UserSaveQuery query)  {
    User user = new User();

    user.setCover(query.getCover());
    user.setName(query.getName());
    user.setEmail(query.getEmail());

    if (query.getId() == null) {
      if (query.getPassword() == null) {
        return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Validator.SaveUser.PASSWORD_REQUIRED));
      }
      user.setPassword(SaSecureUtil.md5(query.getPassword()));
    } else {
      // 编辑的时候，如果有密码，就修改
      if (query.getPassword() != null) {
        user.setPassword(SaSecureUtil.md5(query.getPassword()));
      }
    }

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



    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("email", query.getEmail());
      List<User> list = userMapper.selectList(wrapper);

      if (list.size() == 0) {
        userMapper.insert(user);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.User.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.User.EMAIL_REPEAT));
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
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.User.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.User.EMAIL_REPEAT));
      }
    }
  }
}
