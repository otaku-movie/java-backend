package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Menu;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MenuMapper;

import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
class MenuSaveQuery {
  Integer id;
  @NotEmpty(message = "{validator.saveApi.name.required}")
  String name;
  @NotEmpty(message = "{validator.saveApi.i18nKey.required}")
  String i18nKey;
  @NotEmpty(message = "{validator.saveApi.path.required}")
  String path;
  @NotEmpty(message = "{validator.saveApi.pathName.required}")
  String pathName;
  Boolean show;
  Integer parentId;
}

@Data
class MenuListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;

  public MenuListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class MenuController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MenuMapper menuMapper;

  @PostMapping("/api/admin/permission/menu/list")
  public RestBean<List<Menu>> list(@RequestBody MenuListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    wrapper.orderByDesc("update_time");

    List list = menuMapper.selectList(wrapper);

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
  @GetMapping("/api/admin/permission/menu/detail")
  public RestBean<Menu> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Menu result = menuMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @DeleteMapping("/api/admin/permission/menu/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    menuMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @PostMapping("/api/admin/permission/menu/save")
  public RestBean<List<Object>> save(@RequestBody @Validated MenuSaveQuery query)  {
    Menu data = new Menu();

    data.setName(query.getName());
    data.setPath(query.getPath());
    data.setPathName(query.getPathName());
    data.setShow(query.getShow());
    data.setI18nKey(query.getI18nKey());
    if (query.getParentId() != null) {
      data.setParentId(query.getParentId());
    }

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("path", query.getPath());
      List<Menu> list = menuMapper.selectList(wrapper);

      if (list.size() == 0) {
        menuMapper.insert(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
      }
    } else {
      // 判断编辑是否重复，去掉当前的，如果path已存在就算重复
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("path", query.getPath());
      queryWrapper.ne("id", query.getId());

      List<Menu> menuList = menuMapper.selectList(queryWrapper);

      if (menuList.size() == 0) {
        data.setId(query.getId());
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());
        menuMapper.update(data, updateQueryWrapper);

        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(
          ResponseCode.REPEAT.getCode(),
          MessageUtils.getMessage("error.repeat", MessageUtils.getMessage("repeat.path"))
        );
      }
    }
  }
}
