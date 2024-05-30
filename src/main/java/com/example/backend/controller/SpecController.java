package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.CinemaSpec;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.SpecMapper;
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
class SpecListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;

  public SpecListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class SaveSpecQuery {
  private Integer id;
  @NotNull
  @NotEmpty
  private String name;
  private String description;
}

@RestController
public class SpecController {
  @Autowired
  private SpecMapper specMapper;

  @PostMapping("/api/cinema/spec/list")
  public RestBean<List<Object>> list(@RequestBody SpecListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByDesc("update_time");
    Page<CinemaSpec> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.eq("name", query.getName());
    }
    if (query.getId() != null) {
      wrapper.eq("id", query.getId());
    }

    IPage list = specMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/cinema/spec/detail")
  public RestBean<CinemaSpec> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    CinemaSpec result = specMapper.selectById(id);

    return RestBean.success(result, "获取成功");
  }
  @PostMapping("/api/admin/cinema/spec/save")
  public RestBean<String> save(@RequestBody @Validated() SaveSpecQuery query) {
    CinemaSpec spec = new CinemaSpec();

    spec.setName(query.getName());
    spec.setDescription(query.getDescription());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<CinemaSpec> list = specMapper.selectList(wrapper);

      if (list.size() == 0) {
        specMapper.insert(spec);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前规格已经存在");
      }
    } else {
      spec.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      CinemaSpec old = specMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        specMapper.update(spec, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        CinemaSpec find = specMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前规格已经存在");
        } else {
          specMapper.update(spec, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
  @DeleteMapping("/api/admin/cinema/spec/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    specMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
