package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Position;
import com.example.backend.mapper.PositionMapper;
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
class PositionSaveQuery {
  Integer id;
  @NotNull
  String name;
}


@Data
class PositionListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public PositionListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class PositionController {
  @Autowired
  private PositionMapper positionMapper;

  @PostMapping("/api/position/list")
  public RestBean<List<Position>> list(@RequestBody PositionListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Position> page = new Page<>(query.getPage() - 1, query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId() != 0) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByDesc("update_time");

    IPage list = positionMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping("/api/position/detail")
  public RestBean<Position> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Position> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Position result = positionMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/admin/position/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    positionMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/admin/position/save")
  public RestBean<List<Object>> save(@RequestBody @Validated PositionSaveQuery query)  {
    Position data = new Position();

    data.setName(query.getName());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Position> list = positionMapper.selectList(wrapper);

      if (list.size() == 0) {
        positionMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前角色已经存在");
      }
    } else {
      data.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Position old = positionMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName()) && old.getId() == query.getId()) {
        positionMapper.update(data, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Position find = positionMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前角色已经存在");
        } else {
          positionMapper.update(data, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
}
