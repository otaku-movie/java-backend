package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Movie;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Cinema;
import com.example.backend.mapper.CinemaMapper;
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
class CinemaListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;

  public CinemaListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class SaveCinemaQuery {
  private Integer id;
  @NotNull
  @NotEmpty
  private String name;
  private String description;
  private String address;
  private String homePage;
  private String tel;
}

@RestController
public class CinemaController {
  @Autowired
  private CinemaMapper cinemaMapper;

  @PostMapping("/api/cinema/list")
  public RestBean<List<Object>> list(@RequestBody CinemaListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByAsc("update_time");
    Page<Cinema> page = new Page<>(query.getPage() - 1, query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.eq("name", query.getName());
    }

    IPage list = cinemaMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/cinema/detail")
  public RestBean<Cinema> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    Cinema result = cinemaMapper.selectById(id);

    return RestBean.success(result, "删除成功");
  }
  @PostMapping("/api/cinema/save")
  public RestBean<String> save(@RequestBody @Validated() SaveCinemaQuery query) {
    Cinema cinema = new Cinema();

    cinema.setName(query.getName());
    cinema.setAddress(query.getAddress());
    cinema.setHomePage(query.getHomePage());
    cinema.setTel(query.getTel());
    cinema.setDescription(query.getDescription());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Cinema> list = cinemaMapper.selectList(wrapper);

      if (list.size() == 0) {
        cinemaMapper.insert(cinema);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前影院已经存在");
      }
    } else {
      cinema.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Cinema old = cinemaMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        cinemaMapper.update(cinema, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Cinema find = cinemaMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前影院已经存在");
        } else {
          cinemaMapper.update(cinema, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
  @DeleteMapping("/api/cinema/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    cinemaMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
