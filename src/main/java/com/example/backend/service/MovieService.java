package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.*;
import com.example.backend.entity.Character;
import com.example.backend.query.SaveMovieQuery;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.query.CharacterSaveQuery;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Data
class MovieResponse {
  private  Integer id;
}

@Service
public class MovieService {
  @Autowired
  private GenericService<Movie> genericService;

  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private MovieStaffService movieStaffService;

  @Autowired
  private  MovieCharacterService movieCharacterService;

  public void saveMovie(Movie movie, SaveMovieQuery query) {
    if (query.getId() == null) {
      movieMapper.insert(movie);

    } else  {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      movieMapper.update(movie, updateQueryWrapper);
    }

    if (query.getStaffList() != null) {
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("movie_id", query.getId());
      movieStaffService.remove(queryWrapper);

      movieStaffService.saveBatch(
        query.getStaffList().stream()
          .map(item -> {
            MovieStaff data = new MovieStaff();
            data.setMovieId(query.getId());
            data.setStaffId(item.getStaffId());
            data.setPositionId(item.getPositionId());

            return data;
          })
          .collect(Collectors.toList())
      );
    }
    if (query.getCharacterList() != null) {
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("movie_id", query.getId());
      movieCharacterService.remove(queryWrapper);

      movieCharacterService.saveBatch(
        query.getCharacterList().stream()
          .map(item -> {
            MovieCharacter data = new MovieCharacter();
            data.setMovieId(query.getId());
            data.setCharacterId(item.getCharacterId());

            return data;
          })
          .collect(Collectors.toList())
      );
    }

  }
  public RestBean<Object> save (SaveMovieQuery query) {
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

    // 添加的去重查询条件
    QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("original_name", name);
    // 编辑的去重查询条件
//    UpdateWrapper<Movie> updateWrapper = new UpdateWrapper<>();
//    updateWrapper.eq("original_name", name);
    // 自定义验证方法
    ValidationFunction<Movie> validationFunction = (old, newData) -> old.getOriginalName().equals(newData.getOriginalName());

    boolean result = genericService.validate(
      movie, query.getId(), validationFunction, queryWrapper, movieMapper
    );

    MovieResponse movieResponse = new MovieResponse();

    movieResponse.setId(movie.getId());

    if (query.getId() == null) {
      if (result) {
        saveMovie(movie, query);
        return RestBean.success(movieResponse, "保存成功");
      } else {
        return RestBean.error(0, "数据已存在");
      }
    } else {
      if (result) {
        movie.setId(query.getId());
        saveMovie(movie, query);
        return RestBean.success(movieResponse, "保存成功");
      } else {
        return RestBean.error(0, "数据已存在");
      }
    }
  }
}
