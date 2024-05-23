package com.example.backend.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.*;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieSpecMapper;
import com.example.backend.mapper.SpecMapper;
import com.example.backend.query.MovieListQuery;
import com.example.backend.response.MovieResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;



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
  private String originalName;
  private List<Integer> spec;
}

@RestController
public class MovieController {
  @Autowired
  private MovieMapper movieMapper;
  @Autowired
  private MovieSpecMapper movieSpecMapper;
  @Autowired
  private SpecMapper specMapper;

  @PostMapping("/api/movie/list")
  public RestBean<List<MovieResponse>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByAsc("update_time");
    Page<MovieResponse> page = new Page<>(query.getPage() - 1, query.getPageSize());

//    if (query.getReleaseStatus() != null && query.getReleaseStatus() != 0) {
//      wrapper.eq("status", query.getReleaseStatus());
//    }
//    if (query.getName() != null && query.getName() != "") {
//      wrapper.eq("name", query.getName());
//    }

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
  @PostMapping("/api/movie/save")
  public RestBean<String> save(@RequestBody  @Validated() SaveMovieQuery query)  {
    Movie movie = new Movie();
    String name = query.getOriginalName() == null ? query.getName() : query.getOriginalName();

    if (query.getId() != null) {
      movie.setId(query.getId());
    }

    if (query.getOriginalName() == null) {
      query.setOriginalName(query.getName());
    } else {
      movie.setOriginalName(query.getOriginalName());
    }
    if (query.getStatus() == null) {
      movie.setStatus(1);
    } else {
      movie.setStatus(query.getStatus());
    }
    if (query.getTime() != null) {
      movie.setTime(query.getTime());
    }

    movie.setName(query.getName());
    movie.setDescription(query.getDescription());
    movie.setStartDate(query.getStartDate());
    movie.setEndDate(query.getEndDate());
    movie.setHomePage(query.getHomePage());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("original_name", name);
      List<Cinema> list = movieMapper.selectList(wrapper);

      if (list.size() == 0) {
        Integer id = movieMapper.insert(movie);
        query.getSpec().stream().forEach(item -> {
          MovieSpec spec = new MovieSpec();
          spec.setMovieId(id);
          spec.setSpecId(item);
          movieSpecMapper.insert(spec);
        });
        insertSpec(id, query.getSpec());
        return RestBean.success(null, "success");
      } else {
        return RestBean.error(0, "当前数据已存在");
      }
    } else {
      movie.setId(query.getId());
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Movie old = movieMapper.selectById(query.getId());

      if (
        Objects.equals(old.getOriginalName(), query.getOriginalName()) &&
          old.getId() == query.getId()
      ) {
        movieMapper.update(movie, updateQueryWrapper);
        insertSpec(query.getId(), query.getSpec());
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("original_name", name);
        Movie find = movieMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(0, "当前数据已存在");
        } else {
          movieMapper.update(movie, updateQueryWrapper);
          insertSpec(query.getId(), query.getSpec());
        }
      }
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
