package com.example.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.RestBean;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
  private MovieTicketTypeMapper movieTicketTypeMapper;

  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_LIST)
  public RestBean<List<MovieTicketType>> list(@RequestBody(required = false) MovieTicketTypeListQuery query) {
    if (query == null || query.getCinemaId() == null) {
      return RestBean.success(Collections.emptyList(), MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
    QueryWrapper<MovieTicketType> wrapper = new QueryWrapper<>();
    wrapper.eq("cinema_id", query.getCinemaId());
    wrapper.orderByAsc("create_time");
    List<MovieTicketType> list = movieTicketTypeMapper.selectList(wrapper);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
}
