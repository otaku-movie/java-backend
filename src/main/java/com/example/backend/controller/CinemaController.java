package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

@Data
class CinemaListQuery {
  private Integer page;
  private Integer pageSize;

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
  public RestBean<List<Object>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Cinema> page = new Page<>(query.getPage() - 1, query.getPageSize());

    IPage list = cinemaMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
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
      cinemaMapper.insert(cinema);
    } else {
      cinema.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      cinemaMapper.update(cinema, updateQueryWrapper);
    }

    return RestBean.success(null, "success");
  }
  @DeleteMapping("/api/cinema/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    cinemaMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
