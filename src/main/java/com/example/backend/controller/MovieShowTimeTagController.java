package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.MovieShowTimeTag;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieShowTimeTagMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
class MovieShowTimeTagSaveQuery {
  Integer id;
  @NotEmpty(message = "validator.saveMovieShowTimeTag.name.required")
  String name;
}


@Data
class MovieShowTimeTagListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer id;

  public MovieShowTimeTagListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class MovieShowTimeTagController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieShowTimeTagMapper showTimeTagMapper;

  @PostMapping(ApiPaths.Common.ShowTimeTag.LIST)
  public RestBean<List<MovieShowTimeTag>> list(@RequestBody MovieShowTimeTagListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<MovieShowTimeTag> page = new Page<>(query.getPage(), query.getPageSize());

    if (query.getName() != null && query.getName() != "") {
      wrapper.like("name", query.getName());
    }
    if (query.getId() != null && query.getId() != 0) {
      wrapper.eq("id", query.getId());
    }
    wrapper.orderByDesc("update_time");

    IPage list = showTimeTagMapper.selectPage(page, wrapper);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.Common.ShowTimeTag.DETAIL)
  public RestBean<MovieShowTimeTag> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<MovieShowTimeTag> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieShowTimeTag result = showTimeTagMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "showTimeTag.remove")
  @DeleteMapping(ApiPaths.Admin.ShowTimeTag.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    showTimeTagMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "showTimeTag.save")
  @PostMapping(ApiPaths.Admin.ShowTimeTag.SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated MovieShowTimeTagSaveQuery query) {
    MovieShowTimeTag data = new MovieShowTimeTag();
    data.setName(query.getName());

    QueryWrapper<MovieShowTimeTag> wrapper = new QueryWrapper<>();
    wrapper.eq("name", query.getName());

    String repeatMessage = MessageUtils.getMessage("error.repeat", MessageUtils.getMessage("repeat.showTimeTagName"));

    if (query.getId() == null) {
      // 新增操作
      List<MovieShowTimeTag> list = showTimeTagMapper.selectList(wrapper);
      if (list.isEmpty()) {
        showTimeTagMapper.insert(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      }
    } else {
      // 更新操作
      wrapper.ne("id", query.getId());
      MovieShowTimeTag find = showTimeTagMapper.selectOne(wrapper);

      if (find != null) {
        return RestBean.error(ResponseCode.REPEAT.getCode(), repeatMessage);
      } else {
        data.setId(query.getId());
        showTimeTagMapper.updateById(data);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      }
    }
  }
}
