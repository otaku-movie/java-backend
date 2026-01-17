package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.HelloMovie;
import com.example.backend.entity.Movie;
import com.example.backend.entity.MovieShowTime;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.query.app.AppMovieListQuery;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.app.*;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;



@Data
class  getMovieStaffQuery {
  Integer movieId;
}
@RestController
public class AppMovieController {
  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;

  @Autowired
  private MovieMapper movieMapper;

  @GetMapping(ApiPaths.App.Movie.NOW_SHOWING)
  public RestBean<List<NowMovieShowingResponse>> list(
    @ModelAttribute AppMovieListQuery query
  )  {
    QueryWrapper wrapper = new QueryWrapper<>();
    Page<MovieMapper> page = new Page<>(query.getPage(), query.getPageSize());

    // 第一步：获取基本的电影信息
    IPage<NowMovieShowingResponse> list = movieMapper.nowMovieShowing(query, page);
    
    if (list.getRecords() != null && !list.getRecords().isEmpty()) {
      // 第二步：提取电影ID列表
      List<Integer> movieIds = list.getRecords().stream()
        .map(NowMovieShowingResponse::getId)
        .toList();
      
      // 第三步：批量获取Hello Movie信息
      List<com.example.backend.response.movie.HelloMovie> helloMovies = movieMapper.getHelloMoviesByMovieIds(movieIds);
      
      // 第四步：按电影ID分组Hello Movie信息
      Map<Integer, List<com.example.backend.response.movie.HelloMovie>> helloMovieMap = helloMovies.stream()
        .collect(Collectors.groupingBy(com.example.backend.response.movie.HelloMovie::getMovieId));
      
      // 第五步：组装结果
      list.getRecords().forEach(movie -> {
        List<com.example.backend.response.movie.HelloMovie> movieHelloMovies = helloMovieMap.getOrDefault(movie.getId(), Collections.emptyList());
        movie.setHelloMovie(movieHelloMovies);
      });
    }

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }

  @GetMapping(ApiPaths.App.Movie.STAFF)
  public RestBean<List<AppMovieStaffResponse>> getMovieStaff(
    @ModelAttribute getMovieStaffQuery query
  )  {
    List<AppMovieStaffResponse> list = movieMapper.appMovieStaff(query.getMovieId());

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  @GetMapping(ApiPaths.App.Movie.COMING_SOON)
  public RestBean<List<MovieComingSoonResponse>> getComingSoon (@ModelAttribute AppMovieListQuery query) {
    Page<MovieMapper> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieComingSoonResponse> list = movieMapper.getMovieComingSoon(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
//  获取电影的上映场次
  @PostMapping(ApiPaths.App.Movie.SHOW_TIME)
  public RestBean<Object> showTime (@RequestBody getMovieShowTimeQuery query) {
    Page<MovieShowTimeMapper> page = new Page<>(query.getPage(), query.getPageSize());

    // 如果使用30小时制，将时间转换为24小时制
    if (query.getUse30HourFormat() != null && query.getUse30HourFormat()) {
      if (query.getStartTimeFrom() != null && !query.getStartTimeFrom().isEmpty()) {
        query.setStartTimeFrom(Utils.convert30HourTo24Hour(query.getStartTimeFrom()));
      }
      if (query.getStartTimeTo() != null && !query.getStartTimeTo().isEmpty()) {
        query.setStartTimeTo(Utils.convert30HourTo24Hour(query.getStartTimeTo()));
      }
    }

    // 将时间参数统一提取为 HH:mm 格式，便于 SQL 中进行时间部分比较
    if (query.getStartTimeFrom() != null && !query.getStartTimeFrom().isEmpty()) {
      String timeFrom = Utils.extractTimePart(query.getStartTimeFrom());
      if (timeFrom != null) {
        query.setStartTimeFrom(timeFrom);
      }
    }
    if (query.getStartTimeTo() != null && !query.getStartTimeTo().isEmpty()) {
      String timeTo = Utils.extractTimePart(query.getStartTimeTo());
      if (timeTo != null) {
        query.setStartTimeTo(timeTo);
      }
    }

    List<AppBeforeMovieShowTimeResponse> list = movieMapper.getMovieShowTime(query, ShowTimeState.no_started.getCode(), page);

    // 如果使用30小时制，先将时间转换为30小时制，然后再分组
    if (query.getUse30HourFormat() != null && query.getUse30HourFormat()) {
      for (AppBeforeMovieShowTimeResponse item : list) {
        String startTime = item.getStartTime();
        String endTime = item.getEndTime();
        if (startTime != null) {
          // 如果包含秒，先截取到分钟
          String timeToConvert = startTime.length() >= 16 ? startTime.substring(0, 16) : startTime;
          item.setStartTime(Utils.convert24HourTo30Hour(timeToConvert));
        }
        if (endTime != null) {
          // 如果包含秒，先截取到分钟
          String timeToConvert = endTime.length() >= 16 ? endTime.substring(0, 16) : endTime;
          item.setEndTime(Utils.convert24HourTo30Hour(timeToConvert));
        }
      }
    }

    // 时间范围筛选已在 SQL 中进行，不再需要在 Java 层筛选

    Map<String, List<AppBeforeMovieShowTimeResponse>> map = list.stream().collect(
      Collectors.groupingBy(item -> {
        // 从 start_time 中提取日期部分（yyyy-MM-dd）
        String timeStr = item.getStartTime();
        if (timeStr != null && timeStr.length() >= 10) {
          // 提取日期部分（前10个字符：yyyy-MM-dd）
          return timeStr.substring(0, 10);
        }
        // 如果格式不正确，返回空字符串
        return "";
      })
    );

    List<AppRootMovieShowTimeResponse> result = new ArrayList<>();

    for (String key : map.keySet()) {
      AppRootMovieShowTimeResponse data = new AppRootMovieShowTimeResponse();
      Map<Integer, List<AppBeforeMovieShowTimeResponse>> cinema = map.get(key).stream().collect(Collectors.groupingBy(item -> item.getCinemaId()));

      List<AppMovieShowTimeResponse> cinemaList = new ArrayList<>();

      for (Integer cinemaId : cinema.keySet()) {
        // 获取每个影院的第一条数据
        AppBeforeMovieShowTimeResponse first = cinema.get(cinemaId).get(0);

        // 创建场次信息列表，并按照开始时间排序
        List<ShowTimeInfo> showTimes = cinema.get(cinemaId).stream().map(item -> {
          ShowTimeInfo showTime = new ShowTimeInfo();
          showTime.setId(item.getId());
          showTime.setTheaterHallId(item.getTheaterHallId());
          showTime.setTheaterHallName(item.getTheaterHallName());
          // 时间已经在分组前转换为30小时制了，直接使用
          showTime.setStartTime(item.getStartTime());
          showTime.setEndTime(item.getEndTime());
          showTime.setSpecName(item.getSpecName());
          showTime.setTotalSeats(item.getTotalSeats());
          showTime.setSelectedSeats(item.getSelectedSeats());
          // 计算可用座位数
          Integer availableSeats = item.getTotalSeats() - item.getSelectedSeats();
          showTime.setAvailableSeats(availableSeats);
          showTime.setMovieVersionId(item.getMovieVersionId());
          showTime.setVersionCode(item.getVersionCode());
          return showTime;
        })
        .sorted((t1, t2) -> {
          // 按照开始时间排序
          String startTime1 = t1.getStartTime();
          String startTime2 = t2.getStartTime();
          if (startTime1 == null && startTime2 == null) return 0;
          if (startTime1 == null) return 1;
          if (startTime2 == null) return -1;
          return startTime1.compareTo(startTime2);
        })
        .collect(Collectors.toList());

        // 创建一个新的 AppMovieShowTimeResponse 对象
        AppMovieShowTimeResponse model = new AppMovieShowTimeResponse();
        model.setCinemaId(first.getCinemaId());
        model.setCinemaName(first.getCinemaName());
        model.setCinemaAddress(first.getCinemaAddress());
        model.setCinemaTel(first.getCinemaTel());
        model.setCinemaLatitude(first.getCinemaLatitude());  // 设置影院纬度
        model.setCinemaLongitude(first.getCinemaLongitude());  // 设置影院经度
        model.setTotalShowTimes(showTimes.size());  // 设置总场次数
        model.setDistance(first.getDistance());  // 设置距离
        model.setShowTimes(showTimes);

        // 将模型添加到影院列表中
        cinemaList.add(model);
      }

        // 设置数据
      data.setData(cinemaList);

      data.setDate(key);

      result.add(data);
    }
    List<AppRootMovieShowTimeResponse> sorted = result.stream().sorted((t1, t2) -> {
      String format = "yyyy-MM-dd";
      LocalDate t1Format = Utils.stringToDate(t1.getDate(), format);
      LocalDate t2Format = Utils.stringToDate(t2.getDate(), format);

      return  t1Format.compareTo(t2Format);
    }).toList();
    return RestBean.success(sorted, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

}
