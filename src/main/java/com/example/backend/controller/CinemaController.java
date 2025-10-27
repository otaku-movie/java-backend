package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.TheaterHall;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.TheaterHallMapper;
import com.example.backend.query.CinemaListQuery;
import com.example.backend.query.GetCinemaMovieShowTimeListQuery;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.CinemaResponse;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.app.AppBeforeMovieShowTimeResponse;
import com.example.backend.response.app.AppMovieShowTimeResponse;
import com.example.backend.response.app.GetCinemaMovieShowTimeListResponse;
import com.example.backend.response.cinema.CinemaScreeningResponse;
import com.example.backend.response.cinema.MovieShowingResponse;
import com.example.backend.service.CinemaSpecSpecService;
import com.example.backend.utils.MessageUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Data
class Spec {
    Integer specId;
    Integer plusPrice;
}

@Data
class SaveCinemaQuery {
  private Integer id;
  @NotEmpty(message = "{validator.saveCinema.name.required}")
  private String name;
  @NotEmpty(message = "{validator.saveCinema.description.required}")
  private String description;
  @NotEmpty(message = "{validator.saveCinema.address.required}")
  private String address;
  private String homePage;
  @NotEmpty(message = "{validator.saveCinema.tel.required}")
  private String tel;
  @NotNull(message = "{validator.saveCinema.brandId.required}")
  private Integer brandId;
  private Integer maxSelectSeatCount;
  private List<Spec> spec;

  @NotNull
  private Integer regionId;
  @NotNull
  private Integer prefectureId;
  private Integer cityId;
  @NotEmpty
  private String fullAddress;
}

@RestController
public class CinemaController {
  @Autowired
  private MessageUtils messageUtils;
  @Autowired
  private CinemaMapper cinemaMapper;

  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private CinemaSpecSpecService cinemaSpecSpecService;

  @PostMapping("/api/cinema/list")
  public RestBean<List<CinemaResponse>> list(@RequestBody CinemaListQuery query)  {
    Page<CinemaResponse> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<CinemaResponse> list = cinemaMapper.cinemaList(query, page);
    List<CinemaResponse> result =  list.getRecords().stream().map(item -> {
      List<com.example.backend.response.Spec> spec = cinemaMapper.getCinemaSpec(item.getId());
      item.setSpec(spec);

      return item;
    }).toList();

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping("/api/cinema/detail")
  public RestBean<CinemaResponse> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    CinemaResponse result = cinemaMapper.cinemaDetail(id);
    // 获取影院规格
    List<com.example.backend.response.Spec> spec = cinemaMapper.getCinemaSpec(result.getId());
    if (spec != null) {
      result.setSpec(spec);
    }


    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @PostMapping("/api/app/cinema/movie/showTime")
  public RestBean<Object> showTime (@RequestBody GetCinemaMovieShowTimeListQuery query) {
    GetCinemaMovieShowTimeListResponse list = cinemaMapper.getCinemaMovieShowTimeList(query, ShowTimeState.no_started.getCode());

    // 获取所有场次的字幕和特殊场次标签详细信息
    if (list != null && list.getData() != null) {
      for (var dateGroup : list.getData()) {
        if (dateGroup.getData() != null) {
          for (var theaterHallShowTime : dateGroup.getData()) {
            // 获取字幕信息
            if (theaterHallShowTime.getSubtitleId() != null && !theaterHallShowTime.getSubtitleId().isEmpty()) {
              var subtitles = movieShowTimeMapper.getMovieShowTimeSubtitle(theaterHallShowTime.getSubtitleId());
              theaterHallShowTime.setSubtitle(subtitles);
            }
            
            // 获取特殊场次标签信息
            if (theaterHallShowTime.getShowTimeTagId() != null && !theaterHallShowTime.getShowTimeTagId().isEmpty()) {
              var showTimeTags = movieShowTimeMapper.getMovieShowTimeTags(theaterHallShowTime.getShowTimeTagId());
              theaterHallShowTime.setShowTimeTags(showTimeTags);
            }
          }
        }
      }
    }

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }
  @GetMapping("/api/cinema/screening")
  public RestBean<Object> screening (@RequestParam("id") Integer id, @RequestParam("date") String date) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    QueryWrapper queryWrapper = new QueryWrapper();
    queryWrapper.eq("cinema_id", id);
    List<TheaterHall> theaterHallList = theaterHallMapper.selectList(queryWrapper);

    // 获取当前日期
    LocalDate currentDate = LocalDate.parse(date);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String today = currentDate.format(formatter);

    // 获取今日上映的电影
    MovieShowTimeListQuery movieShowTimeListQuery = new MovieShowTimeListQuery();

    movieShowTimeListQuery.setDate(today);
    movieShowTimeListQuery.setCinemaId(id);
    List<MovieShowTimeList> movieShowTimeListList =  movieShowTimeMapper.movieShowTimeList(movieShowTimeListQuery, OrderState.order_succeed.getCode());

    // 组装返回结果
    List<CinemaScreeningResponse> result = theaterHallList.stream().map(item -> {
      CinemaScreeningResponse cinemaScreeningResponse = new CinemaScreeningResponse();

      cinemaScreeningResponse.setId(item.getId());
      cinemaScreeningResponse.setName(item.getName());
      cinemaScreeningResponse.setDate(today);


      List<MovieShowTimeList> screening = movieShowTimeListList
        .stream()
        .map(movie -> {
          movie.setMovieShowTimeTags(
            movieShowTimeMapper.getMovieShowTimeTags(movie.getMovieShowTimeTagsId())
          );
          movie.setSubtitle(
            movieShowTimeMapper.getMovieShowTimeSubtitle(movie.getSubtitleId())
          );

          return movie;
        })
        .filter(children -> Objects.equals(children.getTheaterHallId(), item.getId()))
        .toList();
      cinemaScreeningResponse.setChildren(screening);

      return cinemaScreeningResponse;
    }).toList();


    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  // 获取影院上映中的电影
  @GetMapping("/api/cinema/movieShowing")
  public RestBean<Object> GetMovieShowing(@RequestParam("id") Integer id) {
    List<MovieShowingResponse> result = cinemaMapper.getMovieShowing(id);

    return RestBean.success(result, MessageUtils.getMessage("success.get"));
  }
  @Transactional
  public void saveCinema(SaveCinemaQuery query) {
    Cinema cinema = new Cinema();

    cinema.setName(query.getName());
    cinema.setAddress(query.getAddress());
    cinema.setHomePage(query.getHomePage());
    cinema.setTel(query.getTel());
    cinema.setDescription(query.getDescription());
    cinema.setMaxSelectSeatCount(query.getMaxSelectSeatCount());

    // 设置地区
    cinema.setRegionId(query.getRegionId());
    cinema.setPrefectureId(query.getPrefectureId());
    cinema.setCityId(query.getCityId());
    cinema.setFullAddress(query.getFullAddress());

    if (query.getBrandId() != null) {
      cinema.setBrandId(query.getBrandId());
    }

    if (query.getId() == null) {
      cinemaMapper.insert(cinema);
    } else {
      cinema.setId(query.getId());
      cinemaMapper.updateById(cinema);
    }

    if (query.getSpec() != null) {
      List<CinemaSpecSpec> spec = query.getSpec().stream().map(item -> {
        CinemaSpecSpec modal = new CinemaSpecSpec();

        modal.setCinemaId(cinema.getId());
        modal.setSpecId(item.getSpecId());
        modal.setPlusPrice(item.getPlusPrice());

        return modal;
      }).toList();

      QueryWrapper queryWrapper = new QueryWrapper();
      queryWrapper.eq("cinema_id", cinema.getId());

      cinemaSpecSpecService.remove(queryWrapper);
      cinemaSpecSpecService.saveBatch(spec);
    }
  }
  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @Transactional
  @PostMapping("/api/admin/cinema/save")
  public RestBean<String> save(@RequestBody @Validated() SaveCinemaQuery query) {
    if (query.getId() == null) {
      QueryWrapper wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Cinema> list = cinemaMapper.selectList(wrapper);

      if (list.size() == 0) {
        saveCinema(query);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
      }
    } else {
      UpdateWrapper updateQueryWrapper = new UpdateWrapper();
      updateQueryWrapper.eq("id", query.getId());
      Cinema old = cinemaMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        saveCinema(query);
        return RestBean.success(null, MessageUtils.getMessage("success.save"));
      } else {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Cinema find = cinemaMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage("error.repeat"));
        } else {
          saveCinema(query);
          return RestBean.success(null, MessageUtils.getMessage("success.save"));
        }
      }
    }
  }
  @SaCheckLogin
  @CheckPermission(code = "cinema.remove")
  @DeleteMapping("/api/admin/cinema/remove")
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

    cinemaMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage("success.remove"));
  }
  @GetMapping("/api/cinema/spec")
  public RestBean<List<com.example.backend.response.Spec>> cinemaSpec (@RequestParam Integer cinemaId) {
      if(cinemaId == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), messageUtils.getMessage("error.parameterError"));

      List<com.example.backend.response.Spec> result = cinemaMapper.getCinemaSpec(cinemaId);

      return RestBean.success(result, MessageUtils.getMessage("success.get"));
    }
}
