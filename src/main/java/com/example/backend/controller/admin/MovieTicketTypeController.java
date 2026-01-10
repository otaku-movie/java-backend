package com.example.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.Menu;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Data
class MovieTicketTypeSaveQuery {
  Integer id;
  @NotEmpty(message = "{validator.saveMovieTicketType.name.required}")
  String name;
  @NotNull(message = "{validator.saveMovieTicketType.price.required}")
  Integer price;
  @NotNull(message = "{validator.saveMovieTicketType.cinemaId.required}")
  Integer cinemaId;
}

@Data
class MovieTicketTypeListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  // 是否平铺
  private Integer cinemaId;

  public MovieTicketTypeListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}

@RestController
public class MovieTicketTypeController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private MovieTicketTypeMapper movieTicketTypeMapper;

  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_LIST)
  public RestBean<List<MovieTicketType>> list(@RequestBody MovieTicketTypeListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());

    wrapper.orderByAsc("create_time");

    List list = movieTicketTypeMapper.selectList(wrapper);

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
  @GetMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_DETAIL)
  public RestBean<MovieTicketType> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));
    QueryWrapper<MovieTicketType> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id);

    MovieTicketType result = movieTicketTypeMapper.selectOne(queryWrapper);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieTicketType.remove")
  @DeleteMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    movieTicketTypeMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @SaCheckLogin
  @CheckPermission(code = "movieTicketType.save")
  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_SAVE)
  public RestBean<List<Object>> save(@RequestBody @Validated MovieTicketTypeSaveQuery query)  {
    String message = MessageUtils.getMessage("error.repeat", MessageUtils.getMessage("repeat.movieTicketTypeName"));
    MovieTicketType modal = new MovieTicketType();

    modal.setName(query.getName());
    modal.setPrice(BigDecimal.valueOf(query.getPrice()));
    modal.setCinemaId(query.getCinemaId());

    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("cinema_id", modal.getCinemaId());
      wrapper.eq("name", query.getName());
      List<MovieTicketType> list = movieTicketTypeMapper.selectList(wrapper);

      if (list.size() == 0) {
        movieTicketTypeMapper.insert(modal);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), message);
      }
    } else {
      // 判断编辑是否重复，去掉当前的，如果path已存在就算重复
      QueryWrapper queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("cinema_id", query.getCinemaId());
      queryWrapper.eq("name", query.getName());
      queryWrapper.ne("id", query.getId());

      List<Menu> data = movieTicketTypeMapper.selectList(queryWrapper);

      if (data.size() == 0) {
        modal.setId(query.getId());
        UpdateWrapper updateQueryWrapper = new UpdateWrapper();
        updateQueryWrapper.eq("id", query.getId());
        movieTicketTypeMapper.update(modal, updateQueryWrapper);

        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(
          ResponseCode.REPEAT.getCode(),
          MessageUtils.getMessage("error.repeat", message)
        );
      }
    }
  }
}
