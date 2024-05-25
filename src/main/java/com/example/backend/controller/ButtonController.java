package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Menu;
import com.example.backend.mapper.MenuMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
class MenuSaveQuery {
  Integer id;
  @NotNull
  String name;
  String path;
  String pathName;
  Boolean show;
  Integer parentId;
}

@Data
class MenuListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  // 是否平铺
  private Boolean flattern;

  public MenuListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
    this.flattern = true;
  }
}

@RestController
public class MenuController {
  @Autowired
  private MenuMapper menuMapper;

  @PostMapping("/api/permission/menu/list")
  public RestBean<List<Menu>> list(@RequestBody MenuListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    if (query.getFlattern()) {
      wrapper.orderByDesc("update_time");

      List list = menuMapper.selectList(wrapper);

      return RestBean.success(list, "获取成功");
    } else  {
      List list = menuMapper.selectList(wrapper);

      return RestBean.success(list, "获取成功");
    }
  }
  @GetMapping("/api/permission/menu/detail")
  public RestBean<Menu> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Menu result = menuMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/permission/menu/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    menuMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/permission/menu/save")
  public RestBean<List<Object>> save(@RequestBody @Validated MenuSaveQuery query)  {
    Menu data = new Menu();

    data.setName(query.getName());
    data.setPath(query.getPath());
    data.setPathName(query.getPathName());
    data.setShow(query.getShow());
    if (query.getParentId() != null) {
      data.setParentId(query.getParentId());
    }

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Menu> list = menuMapper.selectList(wrapper);

      if (list.size() == 0) {
        menuMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前菜单已经存在");
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Menu old = menuMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName()) && old.getId() == query.getId()) {
        menuMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Menu find = menuMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前菜单已经存在");
        } else {
          menuMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
}
