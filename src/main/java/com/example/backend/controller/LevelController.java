package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Level;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.LevelMapper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class LevelListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;

  public LevelListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class SaveLevelQuery {
  private Integer id;
  @NotNull
  @NotEmpty
  private String name;
  private String description;
}

@RestController
public class LevelController {
  @Autowired
  private LevelMapper levelMapper;

  @PostMapping("/api/movie/level/list")
  public RestBean<List<Object>> list(@RequestBody LevelListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByDesc("update_time");
    Page<Level> page = new Page<>(query.getPage() - 1, query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.eq("name", query.getName());
    }
    if (query.getId() != null) {
      wrapper.eq("id", query.getId());
    }

    IPage list = levelMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/level/detail")
  public RestBean<Level> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    Level result = levelMapper.selectById(id);

    return RestBean.success(result, "获取成功");
  }
  @PostMapping("/api/admin/movie/level/save")
  public RestBean<String> save(@RequestBody @Validated() SaveLevelQuery query) {
    Level level = new Level();

    level.setName(query.getName());
    level.setDescription(query.getDescription());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Level> list = levelMapper.selectList(wrapper);

      if (list.size() == 0) {
        levelMapper.insert(level);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前规格已经存在");
      }
    } else {
      level.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Level old = levelMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        levelMapper.update(level, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Level find = levelMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前规格已经存在");
        } else {
          levelMapper.update(level, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
  @DeleteMapping("/api/admin/movie/level/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    levelMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
