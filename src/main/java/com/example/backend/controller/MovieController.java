package com.example.backend.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.*;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.mapper.*;
import com.example.backend.query.MovieListQuery;
import com.example.backend.query.SaveMovieQuery;
import com.example.backend.response.Spec;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.MovieResponse;
import com.example.backend.response.MovieStaffResponse;
import com.example.backend.response.movie.Tags;
import com.example.backend.response.movie.MovieVersionResponse;
import com.example.backend.service.MovieService;
import com.example.backend.service.MovieVersionService;
import com.example.backend.utils.MessageUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@RestController
public class MovieController {
  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private  MovieService movieService;
  @Autowired
  private MovieVersionService movieVersionService;
  // 电影场次
  @Autowired
  private  MovieShowTimeMapper movieShowTimeMapper;
  // 电影场次的选座
  private  SelectSeatMapper selectSeatMapper;
  // 电影评论
  @Autowired
  private  MovieCommentMapper movieCommentMapper;
  // 电影回复
  private  MovieReplyMapper movieReplyMapper;
  //电影演员
  @Autowired
  private MovieStaffMapper movieStaffMapper;
  // 电影角色
  @Autowired
  private MovieCharacterMapper movieCharacterMapper;
  // 电影规格
  @Autowired
  private MovieSpecMapper movieSpecMapper;
  @Autowired
  private SpecMapper specMapper;

  @PostMapping(ApiPaths.Common.Movie.LIST)
  public RestBean<List<MovieResponse>> list(@RequestBody MovieListQuery query)  {
    QueryWrapper wrapper = new QueryWrapper<>();
    wrapper.orderByAsc("update_time");
    Page<MovieResponse> page = new Page<>(query.getPage(), query.getPageSize());

    IPage<MovieResponse> list = movieMapper.movieList(query, page);
    List<MovieResponse> result =  list.getRecords().stream().map(item -> {
        List<Tags> tags = movieMapper.getMovieTags(item.getId());
        List<Spec> spec = movieMapper.getMovieSpec(item.getId());
        List<HelloMovie> helloMovies = movieMapper.getHelloMovie(item.getId());

        item.setTags(tags);
        item.setSpec(spec);
        item.setHelloMovie(helloMovies);
        item.setCommentCount(movieMapper.getMovieCommentCount(item.getId()));
        item.setCinemaCount(movieMapper.getAllCinemaCount(item.getId()));
        item.setTheaterCount(movieMapper.getAllTheaterCount(item.getId()));

        return item;
    }).toList();

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.Movie.SPEC)
  public RestBean<List<CinemaSpec>> spec()  {
    QueryWrapper wrapper = new QueryWrapper<>();

    List<CinemaSpec> list = specMapper.selectList(wrapper);

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
  @GetMapping(ApiPaths.Common.Movie.DETAIL)
  public RestBean<MovieResponse> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.PARAMETER));

    MovieResponse result = movieMapper.movieDetail(id);
    MovieResponse data = movieMapper.getMovieRate(id);

    if (data != null) {
      result.setRate(data.getRate());
      result.setTotalRatings(data.getTotalRatings());
    }

    List<Tags> tags = movieMapper.getMovieTags(result.getId());
    List<Spec> spec = movieMapper.getMovieSpec(result.getId());
    List<HelloMovie> helloMovies = movieMapper.getHelloMovie(result.getId());

    result.setTags(tags);
    result.setSpec(spec);
    result.setHelloMovie(helloMovies);
    result.setCommentCount(movieMapper.getMovieCommentCount(result.getId()));
    result.setCinemaCount(movieMapper.getAllCinemaCount(result.getId()));
    result.setTheaterCount(movieMapper.getAllTheaterCount(result.getId()));


    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
  public void insertSpec (Integer movieId, List<Integer> specData) {
    QueryWrapper deleteWrapper = new QueryWrapper<>();
    deleteWrapper.eq("movie_id", movieId);
    movieSpecMapper.delete(deleteWrapper);
    specData.stream().forEach(item -> {
      MovieSpec spec = new MovieSpec();
      spec.setMovieId(movieId);
      spec.setSpecId(item);
      movieSpecMapper.insert(spec);
    });
  }
  @SaCheckLogin
  @CheckPermission(code = "movie.save")
  @Transactional
  @PostMapping(ApiPaths.Admin.Movie.SAVE)
  public RestBean<Object> save(@RequestBody  @Validated() SaveMovieQuery query)  {
    return movieService.save(query);
  }
  @GetMapping(ApiPaths.Common.Movie.STAFF)
  public RestBean<List<MovieStaffResponse>> staff(@RequestParam Integer id)  {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.PARAMETER));

    List<MovieStaffResponse> result = movieMapper.movieStaffList(id);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
  @GetMapping(ApiPaths.Common.Movie.CHARACTER)
  public RestBean<List<MovieStaffResponse>> character(@RequestParam Integer id)  {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.PARAMETER));

    List<MovieStaffResponse> result = movieMapper.movieCharacterList(id);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
  
  /**
   * 获取电影版本列表（包含角色和演员信息）
   * @param movieId 电影ID
   * @return 电影版本列表
   */
  @GetMapping(ApiPaths.Common.Movie.VERSION_LIST)
  public RestBean<List<MovieVersionResponse>> versionList(@RequestParam Integer movieId) {
    if(movieId == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.PARAMETER));
    
    List<MovieVersionResponse> result = movieVersionService.getMovieVersions(movieId);
    
    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS));
  }
  
  @SaCheckLogin
  @CheckPermission(code = "movie.remove")
  @Transactional
  @DeleteMapping(ApiPaths.Admin.Movie.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.PARAMETER));

    movieMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
}
