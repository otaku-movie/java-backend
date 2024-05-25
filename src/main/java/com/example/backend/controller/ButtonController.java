package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.Button;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.ButtonMapper;
import com.example.backend.response.ButtonResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class ButtonSaveQuery {
  Integer id;
  @NotNull
  String name;
  String code;
  Integer menuId;
  Integer apiId;
}

@Data
class ButtonListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  // 是否平铺
  private Boolean flattern;

  public ButtonListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
    this.flattern = true;
  }
}

@RestController
public class ButtonController {
  @Autowired
  private ButtonMapper buttonMapper;

  @PostMapping("/api/permission/button/list")
  public RestBean<List<ButtonResponse>> list(@RequestBody ButtonListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();

    wrapper.orderByDesc("update_time");

    List list = buttonMapper.buttonList();

    return RestBean.success(list, "获取成功");
  }
  @GetMapping("/api/permission/button/detail")
  public RestBean<Button> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Button> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Button result = buttonMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/permission/button/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    buttonMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/permission/button/save")
  public RestBean<List<Object>> save(@RequestBody @Validated ButtonSaveQuery query)  {
    Button data = new Button();

    data.setName(query.getName());
    data.setCode(query.getCode());
    data.setMenuId(query.getMenuId());
    data.setApiId(query.getApiId());


    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("code", query.getCode());
      List<Button> list = buttonMapper.selectList(wrapper);

      if (list.size() == 0) {
        buttonMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前按钮已经存在");
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Button old = buttonMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName()) && old.getId() == query.getId()) {
        buttonMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("code", query.getCode());
        Button find = buttonMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前按钮已经存在");
        } else {
          buttonMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
}
