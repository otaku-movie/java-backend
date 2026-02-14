package com.example.backend.controller.admin;

import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.RestBean;
import com.example.backend.service.MovieTicketTypeService;
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

/** 按影院查票种列表（管理端/发布场次用），可选按星期/日期/时段过滤 */
@Data
class MovieTicketTypeListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer cinemaId;
  /** 星期几 1=周一…7=周日 */
  private Integer weekday;
  /** 目标日期 YYYY-MM-DD */
  private String targetDate;
  /** 场次开始时间 HH:mm */
  private String startTime;
  /** 场次结束时间 HH:mm */
  private String endTime;
  /** true 时返回全部票种（含已禁用），仅票种管理页用；默认 false，只返回启用的 */
  private Boolean includeDisabled;

  public MovieTicketTypeListQuery() {
    this.page = 1;
    this.pageSize = 10;
  }
}

@RestController
public class MovieTicketTypeController {
  @Autowired
  private MovieTicketTypeService movieTicketTypeService;

  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_LIST)
  public RestBean<List<MovieTicketType>> list(@RequestBody(required = false) MovieTicketTypeListQuery query) {
    if (query == null || query.getCinemaId() == null) {
      return RestBean.success(Collections.emptyList(), MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
    boolean includeDisabled = Boolean.TRUE.equals(query.getIncludeDisabled());
    List<MovieTicketType> list = movieTicketTypeService.listByCinema(
        query.getCinemaId(),
        query.getWeekday(),
        query.getTargetDate(),
        query.getStartTime(),
        query.getEndTime(),
        includeDisabled);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
}
