package com.example.backend.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.*;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieSpecMapper;
import com.example.backend.mapper.SpecMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.SaveMovieQuery;
import com.example.backend.response.MovieResponse;
import com.example.backend.response.MovieStaffResponse;
import com.example.backend.service.MovieService;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@RestController
public class MovieController {
  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private  MovieService movieService;

  @Autowired
  private MovieSpecMapper movieSpecMapper;
  @Autowired
  private SpecMapper specMapper;

  @PostMapping("/api/movie/list")
  public RestBean<List<MovieResponse>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByAsc("update_time");
    Page<MovieResponse> page = new Page<>(query.getPage() - 1, query.getPageSize());

    IPage<MovieResponse> list = movieMapper.movieList(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/movie/spec")
  public RestBean<List<CinemaSpec>> spec()  {
    QueryWrapper wrapper = new QueryWrapper<>();

    List<CinemaSpec> list = specMapper.selectList(wrapper);

    return RestBean.success(list, "获取成功");
  }
  @GetMapping("/api/movie/detail")
  public RestBean<MovieResponse> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    MovieResponse result = movieMapper.movieDetail(id);

    return RestBean.success(result, "删除成功");
  }
  public void insertSpec (Integer movieId, List<Integer> specData) {
    QueryWrapper deleteWrapper = new QueryWrapper<>();
    deleteWrapper.eq("movie_id", movieId);
    movieSpecMapper.delete(deleteWrapper);
    specData.stream().forEach(item -> {
      MovieSpec spec = new MovieSpec();
      spec.setMovieId(movieId);
      spec.setSpecId(item);
      movieSpecMapper.insert(spec);
    });
  }
  @Transactional
  @PostMapping("/api/admin/movie/save")
  public RestBean<Object> save(@RequestBody  @Validated() SaveMovieQuery query)  {
    return movieService.save(query);
  }
  @GetMapping("/api/movie/staff")
  public RestBean<List<MovieStaffResponse>> staff(@RequestParam Integer id)  {
    if(id == null) return RestBean.error(-1, "参数错误");

    List<MovieStaffResponse> result = movieMapper.movieStaffList(id);

    return RestBean.success(result, "获取成功");
  }
  @GetMapping("/api/movie/character")
  public RestBean<List<MovieStaffResponse>> character(@RequestParam Integer id)  {
    if(id == null) return RestBean.error(-1, "参数错误");

    List<MovieStaffResponse> result = movieMapper.movieCharacterList(id);

    return RestBean.success(result, "获取成功");
  }
  @DeleteMapping("/api/movie/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(-1, "参数错误");

    movieMapper.deleteById(id);

    return RestBean.success(null, "删除成功");
  }
}
