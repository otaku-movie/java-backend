package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.entity.*;
import com.example.backend.entity.Character;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.*;
import com.example.backend.query.SaveMovieQuery;
import com.example.backend.utils.MessageUtils;
import com.example.backend.query.CharacterSaveQuery;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private MovieSpecMapper movieSpecMapper;
  @Autowired
  MovieStaffMapper movieStaffMapper;

  @Autowired
  MovieCharacterMapper movieCharacterMapper;

  @Autowired
  private MovieStaffService movieStaffService;

  @Autowired
  private  MovieCharacterService movieCharacterService;

  @Transactional
  public void saveMovie(Movie movie, SaveMovieQuery query) {
    if (query.getId() == null) {
      movieMapper.insert(movie);

    } else  {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      movieMapper.update(movie, updateQueryWrapper);
    }

    if (query.getSpec() != null) {
      movieSpecMapper.deleteSpec(movie.getId());

      query.getSpec().forEach(item -> {
        MovieSpec movieSpec = new MovieSpec();
        movieSpec.setSpecId(item);
        movieSpec.setMovieId(movie.getId());

        movieSpecMapper.insert(movieSpec);
      });
    }
    System.out.println(query.getStaffList());
    if (query.getStaffList() != null) {
      movieStaffMapper.deleteStaff(query.getId());

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
      movieCharacterMapper.deleteCharacter(query.getId());

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


    if (query.getStatus() == null) {
      movie.setStatus(1);
    } else {
      movie.setStatus(query.getStatus());
    }
    if (query.getTime() != null) {
      movie.setTime(query.getTime());
    }

    if (query.getLevelId() != null) {
      movie.setLevelId(query.getLevelId());
    }

    movie.setOriginalName(query.getOriginalName());
    movie.setCover(query.getCover());
    movie.setName(query.getName());
    movie.setDescription(query.getDescription());
    movie.setStartDate(query.getStartDate());
    movie.setEndDate(query.getEndDate());
    movie.setHomePage(query.getHomePage());

    // 添加的去重查询条件
    QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("name", name);
    // 编辑的去重查询条件
    // 自定义验证方法
    ValidationFunction<Movie> validationFunction = (old, newData) -> old.getName().equals(newData.getName());

    boolean result = genericService.validate(
      movie, query.getId(), validationFunction, queryWrapper, movieMapper
    );

    MovieResponse movieResponse = new MovieResponse();

    if (query.getId() == null) {
      if (result) {
        saveMovie(movie, query);
        movieResponse.setId(movie.getId());
        return RestBean.success(movie, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
      }
    } else {
      if (result) {
        movie.setId(query.getId());
        saveMovie(movie, query);
        return RestBean.success(movie, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
      }
    }
  }
}
