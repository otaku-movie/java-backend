package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.MovieTag;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieTagMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class MovieTagSaveQuery {
  Integer id;
  @NotEmpty(message = "validator.saveMovieTag.name.required")
  String name;
}


@Data
class MovieTagListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public MovieTagListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class MovieTagController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieTagMapper movieTagMapper;

  @PostMapping(ApiPaths.Common.MovieTag.LIST)
  public RestBean<List<MovieTag>> list(@RequestBody MovieTagListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<MovieTag> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId() != 0) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByDesc("update_time");

    IPage list = movieTagMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.Common.MovieTag.DETAIL)
  public RestBean<MovieTag> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    QueryWrapper<MovieTag> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieTag result = movieTagMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieTag.remove")
  @DeleteMapping(ApiPaths.Admin.Tag.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    movieTagMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieTag.save")
  @PostMapping(ApiPaths.Admin.Tag.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated MovieTagSaveQuery query) {
    MovieTag data = new MovieTag();
    data.setName(query.getName());

    QueryWrapper<MovieTag> wrapper = new QueryWrapper<>();
    wrapper.eq("name", query.getName());

    String repeatMessage = MessageUtils.getMessage(MessageKeys.Admin.REPEAT_ERROR, MessageUtils.getMessage(MessageKeys.Admin.Repeat.MOVIE_TAG_NAME));

    if (query.getId() == null) {
      // 新增操作
      List<MovieTag> list = movieTagMapper.selectList(wrapper);
      if (list.isEmpty()) {
        movieTagMapper.insert(data);
        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 更新操作
      wrapper.ne("id", query.getId());
      MovieTag find = movieTagMapper.selectOne(wrapper);

      if (find != null) {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      } else {
        data.setId(query.getId());
        movieTagMapper.updateById(data);
        return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
      }
    }
  }
}
