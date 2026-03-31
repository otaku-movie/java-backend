package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
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
  @NotEmpty(message = "{validator.saveMenu.name.required}")
  String i18nKey;
  @NotEmpty(message = "{validator.saveMenu.path.required}")
  String path;
  @NotEmpty(message = "{validator.saveMenu.pathName.required}")
  String pathName;
  Boolean show;
  Integer parentId;
}

@Data
class MenuReorderItem {
  Integer id;
  Integer parentId;
  Integer orderNum;
}

@Data
class MenuReorderQuery {
  List<MenuReorderItem> items;
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

  @PostMapping(ApiPaths.Admin.Menu.LIST)
  public RestBean<List<Menu>> list(@RequestBody MenuListQuery query)  {
    QueryWrapper<Menu> wrapper = new QueryWrapper<>();

    // 为了兼容旧数据库：order_num 可能尚未落库。
    // 优先按 (parent_id, order_num) 排序并取 order_num；若字段不存在则回退到 update_time 排序。
    List list;
    try {
      wrapper.select("id", "i18n_key", "name", "show", "path", "path_name", "parent_id", "order_num", "create_time", "update_time")
          .orderByAsc("parent_id")
          .orderByAsc("order_num");
      list = menuMapper.selectList(wrapper);
    } catch (Exception ignored) {
      wrapper = new QueryWrapper<>();
      wrapper.select("id", "i18n_key", "name", "show", "path", "path_name", "parent_id", "create_time", "update_time")
          .orderByDesc("update_time");
      list = menuMapper.selectList(wrapper);
    }

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @GetMapping(ApiPaths.Admin.Menu.DETAIL)
  public RestBean<Menu> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Menu result = menuMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "menu.remove")
  @DeleteMapping(ApiPaths.Admin.Menu.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    menuMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "menu.save")
  @PostMapping(ApiPaths.Admin.Menu.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated MenuSaveQuery query)  {
    Menu data = new Menu();

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
        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      } else {
        String repeatMessage = MessageUtils.getMessage(
          MessageKeys.Admin.REPEAT_ERROR,
          MessageUtils.getMessage(MessageKeys.Admin.Repeat.PATH)
        );
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 判断编辑是否重复，去掉当前的
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("path", query.getPath());
      queryWrapper.ne("id", query.getId());

      List<Menu> menuList = menuMapper.selectList(queryWrapper);

      if (menuList.size() == 0) {
        data.setId(query.getId());
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());
        menuMapper.update(data, updateQueryWrapper);

        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
      } else {
        String repeatMessage = MessageUtils.getMessage(
          MessageKeys.Admin.REPEAT_ERROR,
          MessageUtils.getMessage(MessageKeys.Admin.Repeat.PATH)
        );
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    }
  }

  @SaCheckLogin
  @CheckPermission(code = "menu.save")
  @PostMapping(ApiPaths.Admin.Menu.REORDER)
  public RestBean<Null> reorder(@RequestBody MenuReorderQuery query) {
    if (query == null || query.getItems() == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }

    for (MenuReorderItem item : query.getItems()) {
      if (item == null || item.getId() == null) continue;
      try {
        Menu data = new Menu();
        data.setParentId(item.getParentId());
        data.setOrderNum(item.getOrderNum());

        QueryWrapper<Menu> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("id", item.getId());
        menuMapper.update(data, updateWrapper);
      } catch (Exception ignored) {
        // 旧库兼容：order_num 列不存在时，至少更新 parent_id 以保证层级拖拽生效
        Menu data = new Menu();
        data.setParentId(item.getParentId());
        QueryWrapper<Menu> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("id", item.getId());
        menuMapper.update(data, updateWrapper);
      }
    }

    return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }
}
