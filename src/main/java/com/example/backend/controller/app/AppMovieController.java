package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

  @GetMapping("/api/app/movie/nowShowing")
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

  @GetMapping("/api/app/movie/staff")
  public RestBean<List<AppMovieStaffResponse>> getMovieStaff(
    @ModelAttribute getMovieStaffQuery query
  )  {
    List<AppMovieStaffResponse> list = movieMapper.appMovieStaff(query.getMovieId());

    return RestBean.success(list, MessageUtils.getMessage("success.get"));
  }

  @GetMapping("/api/app/movie/comingSoon")
  public RestBean<List<MovieComingSoonResponse>> getComingSoon (@ModelAttribute AppMovieListQuery query) {
    Page<MovieMapper> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieComingSoonResponse> list = movieMapper.getMovieComingSoon(query, page);

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
//  获取电影的上映场次
  @PostMapping("/api/app/movie/showTime")
  public RestBean<Object> showTime (@RequestBody getMovieShowTimeQuery query) {
    Page<MovieShowTimeMapper> page = new Page<>(query.getPage(), query.getPageSize());

    List<AppBeforeMovieShowTimeResponse> list = movieMapper.getMovieShowTime(query, ShowTimeState.no_started.getCode(), page);

    Map<String, List<AppBeforeMovieShowTimeResponse>> map = list.stream().collect(
      Collectors.groupingBy(item -> {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
          // 将 start_time 字符串解析为 Date 对象
          Date parsedDate = format.parse(item.getStartTime());
          // 返回 Date 对象作为分组的键
          return format.format(parsedDate);  // 如果需要返回字符串，可以使用 format.format(parsedDate)
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
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

        // 创建场次信息列表
        List<ShowTimeInfo> showTimes = cinema.get(cinemaId).stream().map(item -> {
          ShowTimeInfo showTime = new ShowTimeInfo();
          showTime.setId(item.getId());
          showTime.setTheaterHallId(item.getTheaterHallId());
          showTime.setTheaterHallName(item.getTheaterHallName());
          showTime.setStartTime(item.getStartTime());
          showTime.setEndTime(item.getEndTime());
          showTime.setSpecName(item.getSpecName());
          showTime.setTotalSeats(item.getTotalSeats());
          showTime.setSelectedSeats(item.getSelectedSeats());
          // 计算可用座位数
          Integer availableSeats = item.getTotalSeats() - item.getSelectedSeats();
          showTime.setAvailableSeats(availableSeats);
          return showTime;
        }).collect(Collectors.toList());

        // 创建一个新的 AppMovieShowTimeResponse 对象
        AppMovieShowTimeResponse model = new AppMovieShowTimeResponse();
        model.setCinemaId(first.getCinemaId());
        model.setCinemaName(first.getCinemaName());
        model.setCinemaAddress(first.getCinemaAddress());
        model.setCinemaTel(first.getCinemaTel());
        model.setTotalShowTimes(showTimes.size());  // 设置总场次数
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
    return RestBean.success(sorted, MessageUtils.getMessage("success.get"));
  }

}
