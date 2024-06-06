package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.response.ButtonResponse;
import com.example.backend.response.Button;
import com.example.backend.service.RoleButtonService;
import com.example.backend.service.RoleMenuService;
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
class RoleSaveQuery {
  Integer id;
  @NotNull
  String name;
}

@Data
class RoleConfigQuery {
  @NotNull
  Integer roleId;
  @NotNull
  List<Integer> menuId;
  @NotNull
  List<Integer> buttonId;
}

@Data
class RoleListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;

  public RoleListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

// 工具类，处理过滤逻辑
class ButtonResponseProcessor {

  public static ButtonResponse filterButtonResponse(ButtonResponse response) {
    // 过滤按钮列表，保留 checked 为 true 的按钮
    List<Button> filteredButtons = response.getButton().stream()
      .filter(Button::getChecked)
      .collect(Collectors.toList());

    response.setButton(filteredButtons);

    // 递归处理子节点
    if (response.getChildren() != null) {
      List<ButtonResponse> filteredChildren = response.getChildren().stream()
        .map(ButtonResponseProcessor::filterButtonResponse)
        .collect(Collectors.toList());
      response.setChildren(filteredChildren);
    }

    return response;
  }
}

@RestController
public class RoleController {
  @Autowired
  private RoleMapper roleMapper;

  @Autowired
  private RoleMenuService roleMenuService;

  @Autowired
  private RoleButtonService roleButtonService;

  @PostMapping("/api/admin/permission/role/list")
  public RestBean<List<Role>> list(@RequestBody RoleListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Role> page = new Page<>(query.getPage(), query.getPageSize());

    wrapper.orderByDesc("update_time");

    IPage list = roleMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/admin/permission/role/permissionList")
  public RestBean<List<ButtonResponse>> permissionList(@RequestParam Integer id)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    List list = roleMapper.permissionList(id);

    return RestBean.success(list, "获取成功");
  }
  @GetMapping("/api/admin/permission/role/permission")
  public RestBean<List<ButtonResponse>> permission(@RequestParam Integer id)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    List<ButtonResponse> list = roleMapper.rolePermission(id);
    List<ButtonResponse> filteredList = list.stream()
      .map(ButtonResponseProcessor::filterButtonResponse)
      .collect(Collectors.toList());

    return RestBean.success(filteredList, "获取成功");
  }
  @SaCheckLogin
  @CheckPermission(code = "role.configPermission")
  @Transactional
  @PostMapping("/api/admin/permission/role/config")
  public RestBean<Null> config(@RequestBody @Validated RoleConfigQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("role_id", query.getRoleId());
    roleMenuService.remove(wrapper);
    roleButtonService.remove(wrapper);

    roleMenuService.saveBatch(
      query.menuId.stream()
        .map(item -> {
          RoleMenu roleMenu = new RoleMenu();
          roleMenu.setRoleId(query.getRoleId());
          roleMenu.setMenuId(item);
          return roleMenu;
        })
        .collect(Collectors.toList())  // Collect to List
    );

    roleButtonService.saveBatch(
      query.buttonId.stream()
        .map(item -> {
          RoleButton roleButton = new RoleButton();
          roleButton.setRoleId(query.getRoleId());
          roleButton.setButtonId(item);
          return roleButton;
        })
        .collect(Collectors.toList())  // Collect to List
    );

    return RestBean.success(null, "保存成功");
  }
  @GetMapping("/api/admin/permission/role/detail")
  public RestBean<Role> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Role result = roleMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @SaCheckLogin
  @CheckPermission(code = "role.remove")
  @DeleteMapping("/api/admin/permission/role/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    roleMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @SaCheckLogin
  @CheckPermission(code = "role.save")
  @PostMapping("/api/admin/permission/role/save")
  public RestBean<List<Object>> save(@RequestBody @Validated RoleSaveQuery query)  {
    Role data = new Role();

    data.setName(query.getName());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Role> list = roleMapper.selectList(wrapper);

      if (list.size() == 0) {
        roleMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前角色已经存在");
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Role old = roleMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName()) && old.getId() == query.getId()) {
        roleMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Role find = roleMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前角色已经存在");
        } else {
          roleMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
}
