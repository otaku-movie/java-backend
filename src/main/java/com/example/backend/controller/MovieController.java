package com.example.backend.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.Movie;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieMapper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Data
class MovieListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer releaseStatus;
  private String name;

  public MovieListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@Data
class SaveMovieQuery {
  private Integer id;
  private String cover;
  @NotNull
  @NotEmpty
  private String name;
  @NotEmpty
  private String description;
  private String startDate;
  private String endDate;
  private Integer status;
  private Integer time;
  private String HomePage;
}

@RestController
public class MovieController {
  @Autowired
  private MovieMapper movieMapper;

  @PostMapping("/api/movie/list")
  public RestBean<List<Movie>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<Movie> page = new Page<>(query.getPage() - 1, query.getPageSize());

    if (query.getReleaseStatus() != null && query.getReleaseStatus() != 0) {
      wrapper.eq("status", query.getReleaseStatus());
    }
    if (query.getName() != null && query.getName() != "") {
      wrapper.eq("name", query.getName());
    }

    IPage<Movie> list = movieMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/detail")
  public RestBean<Movie> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    Movie result = movieMapper.selectById(id);

    return RestBean.success(result, "删除成功");
  }
  @PostMapping("/api/movie/save")
  public RestBean<String> save(@RequestBody  @Validated() SaveMovieQuery query)  {
    Movie movie = new Movie();

    if (query.getId() != null) {
      movie.setId(query.getId());
    }

    movie.setName(query.getName());
    movie.setStartDate(query.getStartDate());
    movie.setEndDate(query.getEndDate());
    movie.setTime(query.getTime());
    movie.setStatus(query.getStatus());
    movie.setHomePage(query.getHomePage());

    if (query.getId() == null) {
      movieMapper.insert(movie);
    } else {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      movieMapper.update(movie, updateQueryWrapper);
    }

    return RestBean.success(null, "success");
  }
  @DeleteMapping("/api/movie/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    movieMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
