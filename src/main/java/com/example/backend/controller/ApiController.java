package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Api;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Api;
import com.example.backend.mapper.ApiMapper;
import com.example.backend.mapper.ApiMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class ApiSaveQuery {
  Integer id;
  String name;
  @NotNull
  String path;
}

@Data
class ApiListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;

  public ApiListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class ApiController {
  @Autowired
  private ApiMapper apiMapper;

  @PostMapping("/api/permission/api/list")
  public RestBean<List<Object>> list(@RequestBody ApiListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Api> page = new Page<>(query.getPage() - 1, query.getPageSize());

    wrapper.orderByDesc("update_time");

    IPage list = apiMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/permission/api/detail")
  public RestBean<Api> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Api> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Api result = apiMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/permission/api/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    apiMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/permission/api/save")
  public RestBean<List<Object>> save(@RequestBody @Validated ApiSaveQuery query)  {
    Api data = new Api();

    data.setName(query.getName());
    data.setPath(query.getPath());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("path", query.getPath());
      List<Api> list = apiMapper.selectList(wrapper);

      if (list.size() == 0) {
        apiMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前接口已经存在");
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Api old = apiMapper.selectById(query.getId());

      if (Objects.equals(old.getPath(), query.getPath()) && old.getId() == query.getId()) {
        apiMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("path", query.getPath());
        Api find = apiMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前接口已经存在");
        } else {
          apiMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }

}
