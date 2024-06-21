package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.Movie;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Cinema;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.utils.MessageUtils;
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
  private Integer id;
  private String name;

  public CinemaListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class SaveCinemaQuery {
  private Integer id;
  @NotEmpty(message = "{validator.saveCinema.name.required}")
  private String name;
  @NotEmpty(message = "{validator.saveCinema.description.required}")
  private String description;
  @NotEmpty(message = "{validator.saveCinema.address.required}")
  private String address;
  private String homePage;
  @NotEmpty(message = "{validator.saveCinema.tel.required}")
  private String tel;
  private Integer maxSelectSeatCount;
}

@RestController
public class CinemaController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private CinemaMapper cinemaMapper;

  @PostMapping("/api/cinema/list")
  public RestBean<List<Object>> list(@RequestBody CinemaListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByAsc("update_time");
    Page<Cinema> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.eq("name", query.getName());
    }
    if (query.getId() != null) {
      wrapper.eq("id", query.getId());
    }

    IPage list = cinemaMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/cinema/detail")
  public RestBean<Cinema> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    Cinema result = cinemaMapper.selectById(id);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @PostMapping("/api/admin/cinema/save")
  public RestBean<String> save(@RequestBody @Validated() SaveCinemaQuery query) {
    Cinema cinema = new Cinema();

    cinema.setName(query.getName());
    cinema.setAddress(query.getAddress());
    cinema.setHomePage(query.getHomePage());
    cinema.setTel(query.getTel());
    cinema.setDescription(query.getDescription());
    cinema.setMaxSelectSeatCount(query.getMaxSelectSeatCount());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Cinema> list = cinemaMapper.selectList(wrapper);

      if (list.size() == 0) {
        cinemaMapper.insert(cinema);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
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
          return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
        } else {
          cinemaMapper.update(cinema, updateQueryWrapper);
        }
      }

      return RestBean.success(null, MessageUtils.getMessage("success.save"));
    }
  }
  @SaCheckLogin
  @CheckPermission(code = "cinema.remove")
  @DeleteMapping("/api/admin/cinema/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    cinemaMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
}
