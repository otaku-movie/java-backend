package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Staff;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.StaffMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Data
class StaffSaveQuery {
  Integer id;
  @NotNull
  String name;
  String cover;
  String originalName;
  @NotNull
  String description;
}


@Data
class StaffListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private List<Integer> id;

  public StaffListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class StaffController {
  @Autowired
  private StaffMapper staffMapper;

  @PostMapping("/api/staff/list")
  public RestBean<List<Staff>> list(@RequestBody StaffListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Staff> page = new Page<>(query.getPage(), query.getPageSize());

    wrapper.orderByDesc("update_time");
    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId().size() != 0) {
      wrapper.in("id", query.getId());
    }

    IPage list = staffMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping("/api/staff/detail")
  public RestBean<Staff> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");
    QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    Staff result = staffMapper.selectOne(queryWrapper);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/admin/staff/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    staffMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
  @PostMapping("/api/admin/staff/save")
  public RestBean<List<Object>> save(@RequestBody @Validated StaffSaveQuery query)  {
    Staff data = new Staff();

    data.setName(query.getName());
    data.setDescription(query.getDescription());
    data.setCover(query.getCover());
    data.setOriginalName(query.getOriginalName());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Staff> list = staffMapper.selectList(wrapper);

      if (list.size() == 0) {
        staffMapper.insert(data);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前名称已经存在");
      }
    } else {
      data.setId(query.getId());
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      wrapper.ne("id", query.getId());

      Long count = staffMapper.selectCount(wrapper);

      if (count == 0) {
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());
        staffMapper.update(data, updateQueryWrapper);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前名称已经存在");
      }

//
//
//
//      Staff old = staffMapper.selectById(query.getId());
//
//      if (old.getId() == query.getId()) {
//
//      } else {
//        QueryWrapper wrapper = new QueryWrapper<>();
//        wrapper.eq("name", query.getName());
//        Staff find = staffMapper.selectOne(wrapper);
//
//        if (find != null) {
//
//        } else {
//          staffMapper.update(data, updateQueryWrapper);
//        }
//      }
    }
  }
}
