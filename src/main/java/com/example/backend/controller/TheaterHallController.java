package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.Movie;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.TheaterHall;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.mapper.SelectSeatMapper;
import com.example.backend.mapper.TheaterHallMapper;
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
class TheaterHallQuery {
  private Integer page;
  private Integer pageSize;
  private Integer cinemaId;
  private String name;

  public TheaterHallQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class TheaterHallSaveQuery {
  @NotNull(message = "cinemeId 不能为空")
  private Integer cinemaId;
  private Integer id;
  @NotNull(message = "name 不能为空")
  private String name;
}

@RestController
public class TheaterHallController {
  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private SeatMapper seatMapper;

  @Autowired
  private SelectSeatMapper selectSeatMapper;

  @PostMapping("/api/theater/hall/list")
  public RestBean<List<TheaterHall>> list(@RequestBody TheaterHallQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());
    wrapper.orderByAsc("update_time");
    Page<TheaterHall> page = new Page<>(query.getPage(), query.getPageSize());

    IPage list = theaterHallMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/theater/hall/detail")
  public RestBean<TheaterHall> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    TheaterHall result = theaterHallMapper.selectById(id);

    return RestBean.success(result, "删除成功");
  }
  @GetMapping("/api/theater/hall/seat")
  public RestBean<Object> seatList(@RequestParam Integer theaterHallId) {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("theater_hall_id", theaterHallId);

    List list = seatMapper.selectList(wrapper);

    return RestBean.success(list, "获取成功");
  }
  @PostMapping("/api/admin/theater/hall/save")
  public RestBean<String> save(@RequestBody @Validated() TheaterHallSaveQuery query) {
    TheaterHall theaterHall = new TheaterHall();

    theaterHall.setName(query.getName());
    theaterHall.setCinemaId(query.getCinemaId());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Cinema> list = theaterHallMapper.selectList(wrapper);

      if (list.size() == 0) {
        theaterHallMapper.insert(theaterHall);
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前数据已存在");
      }
    } else {
      theaterHall.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      TheaterHall old = theaterHallMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        theaterHallMapper.update(theaterHall, updateQueryWrapper);
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        TheaterHall find = theaterHallMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前数据已存在");
        } else {
          theaterHallMapper.update(theaterHall, updateQueryWrapper);
        }
      }

      return RestBean.success(null, "success");
    }
  }
  @DeleteMapping("/api/admin/theater/hall/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    return RestBean.success(null, "success");
  }
}
