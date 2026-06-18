package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.CheckPermission;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.CinemaPriceConfig;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.MovieTicketType;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.TheaterHall;
import com.example.backend.enumerate.OrderState;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.CinemaPriceConfigMapper;
import com.example.backend.mapper.MovieShowTimeMapper;
import com.example.backend.mapper.MovieTicketTypeMapper;
import com.example.backend.mapper.TheaterHallMapper;
import com.example.backend.query.CinemaListQuery;
import com.example.backend.query.GetCinemaMovieShowTimeListQuery;
import com.example.backend.query.MovieShowTimeListQuery;
import com.example.backend.response.CinemaResponse;
import com.example.backend.response.MovieShowTimeList;
import com.example.backend.response.app.GetCinemaMovieShowTimeListResponse;
import com.example.backend.response.cinema.CinemaScreeningResponse;
import com.example.backend.response.cinema.MovieShowingResponse;
import com.example.backend.service.BenefitService;
import com.example.backend.service.CinemaServiceDayService;
import com.example.backend.service.CinemaSpecSpecService;
import com.example.backend.utils.ManualFieldLockUtils;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.TagI18nUtils;
import com.example.backend.utils.Utils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
class Spec {
    Integer specId;
    Integer plusPrice;
}

@Data
class TicketTypeItem {
    private Integer id;
    private String name;
    private Integer price;
    private String description;
    private Boolean enabled;
    private Integer scheduleType;
    /** 适用星期几：前端可传 "1,2,3" 或 [1,2,3]，保存时转为 List，scheduleType=周 时使用 */
    private Object applicableWeekdays;
    /** 每月几号 1-31，前端可传 [1,15,28]，scheduleType=月 时使用 */
    private Object applicableMonthDays;
    /** 特定日期 YYYY-MM-DD 数组，scheduleType=特定日期 时使用 */
    private Object applicableDates;
    /** 每日生效时段 开始 HH:mm，scheduleType=每日 时使用 */
    private String dailyStartTime;
    /** 每日生效时段 结束 HH:mm，scheduleType=每日 时使用 */
    private String dailyEndTime;
}

@Data
class PriceConfigItem {
    private Integer dimensionType;
    private java.math.BigDecimal surcharge;
}

@Data
class CinemaTicketTypeSaveQuery {
  @NotNull(message = "{validator.saveCinema.cinemaId.required}")
  private Integer cinemaId;
  private TicketTypeItem ticketType;
}

@Data
class CinemaTicketTypeReorderQuery {
  private Integer cinemaId;
  private List<Integer> ticketTypeIds;
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
  private List<TicketTypeItem> ticketType;
  private List<PriceConfigItem> priceConfig;

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
  private CinemaMapper cinemaMapper;

  @Autowired
  private TheaterHallMapper theaterHallMapper;
  @Autowired
  private MovieShowTimeMapper movieShowTimeMapper;
  @Autowired
  private MovieTicketTypeMapper movieTicketTypeMapper;
  @Autowired
  private CinemaPriceConfigMapper cinemaPriceConfigMapper;

  @Autowired
  private CinemaSpecSpecService cinemaSpecSpecService;
  @Autowired
  private BenefitService benefitService;
  @Autowired
  private com.example.backend.service.FavoriteCinemaService favoriteCinemaService;
  @Autowired
  private CinemaServiceDayService cinemaServiceDayService;

  @PostMapping(ApiPaths.Common.Cinema.LIST)
  public RestBean<List<CinemaResponse>> list(@RequestBody CinemaListQuery query)  {
    Page<CinemaResponse> page = new Page<>(query.getPage(), query.getPageSize());

    // 收藏影院置顶：登录用户注入 userId（未登录为 null，排序不变）
    query.setUserId(favoriteCinemaService.currentUserIdOrNull());

    IPage<CinemaResponse> list = cinemaMapper.cinemaList(query, page);
    List<CinemaResponse> result =  list.getRecords().stream().map(item -> {
      // 获取影院规格
      List<com.example.backend.response.Spec> spec = cinemaMapper.getCinemaSpec(item.getId());
      item.setSpec(spec);
      
      // 获取当前上映的电影
      List<MovieShowingResponse> nowShowingMovies = cinemaMapper.getMovieShowing(item.getId());
      if (!nowShowingMovies.isEmpty()) {
        List<Integer> movieIds = nowShowingMovies.stream().map(MovieShowingResponse::getId).toList();
        java.util.Map<Integer, Boolean> hasBenefitsMap = benefitService.hasBenefitsForMovies(movieIds, LocalDate.now().toString());
        nowShowingMovies.forEach(m -> m.setHasBenefits(Boolean.TRUE.equals(hasBenefitsMap.get(m.getId()))));
      }
      item.setNowShowingMovies(nowShowingMovies);

      return item;
    }).toList();

    return RestBean.success(result, query.getPage(), list.getTotal(), query.getPageSize());
  }
  @GetMapping(ApiPaths.Common.Cinema.DETAIL)
  public RestBean<CinemaResponse> detail (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    CinemaResponse result = cinemaMapper.cinemaDetail(id);
    // 获取影院规格（IMAX / 4DX / Dolby / Screen X 等）
    List<com.example.backend.response.Spec> spec = cinemaMapper.getCinemaSpec(result.getId());
    if (spec == null) {
      spec = new java.util.ArrayList<>();
    } else {
      spec = new java.util.ArrayList<>(spec);
    }

    // 追加 3D 加价：3D 加价存于 cinema_price_config(dimension_type=2)，不在 cinema_spec_spec 表，
    // 详情页要展示给用户，统一并到 spec 列表里返回（票价计算路径仍走 TicketPriceService，不受影响）。
    java.math.BigDecimal surcharge3d =
        cinemaPriceConfigMapper.getSurcharge(result.getId(), 2);
    if (surcharge3d != null && surcharge3d.signum() > 0) {
      com.example.backend.response.Spec spec3d = new com.example.backend.response.Spec();
      spec3d.setName("3D");
      spec3d.setDescription("3D 放映加价");
      spec3d.setPlusPrice(surcharge3d.stripTrailingZeros().toPlainString());
      spec.add(0, spec3d);
    }
    result.setSpec(spec);

    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @PostMapping(ApiPaths.App.Cinema.MOVIE_SHOW_TIME)
  public RestBean<Object> showTime (@RequestBody GetCinemaMovieShowTimeListQuery query) {
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

    GetCinemaMovieShowTimeListResponse list = cinemaMapper.getCinemaMovieShowTimeList(query, ShowTimeState.no_started.getCode());

    // 时间范围筛选已在 SQL 中进行，不再需要在 Java 层筛选

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
              TagI18nUtils.translateShowTimeTags(showTimeTags);
              theaterHallShowTime.setShowTimeTags(showTimeTags);
            }
            
            // 如果使用30小时制，将时间从24小时制转换为30小时制
            if (query.getUse30HourFormat() != null && query.getUse30HourFormat()) {
              String startTime = theaterHallShowTime.getStartTime();
              String endTime = theaterHallShowTime.getEndTime();
              // CinemaMapper 返回的时间格式是 HH:mm，需要拼接日期
              // 使用 dateGroup 的日期来构建完整时间
              if (startTime != null && dateGroup.getDate() != null) {
                String fullStartTime = dateGroup.getDate() + " " + startTime;
                String convertedTime = Utils.convert24HourTo30Hour(fullStartTime);
                theaterHallShowTime.setStartTime(Utils.extractTimePart(convertedTime)); // 只取时间部分
              }
              if (endTime != null && dateGroup.getDate() != null) {
                String fullEndTime = dateGroup.getDate() + " " + endTime;
                String convertedTime = Utils.convert24HourTo30Hour(fullEndTime);
                theaterHallShowTime.setEndTime(Utils.extractTimePart(convertedTime)); // 只取时间部分
              }
            }
          }
          
          // 同一物理场次（同厅 + 开始/结束时间 + 维度 + 版本 + 规格）爬虫可能落库多条，
          // 典型表现是「预售中(pre_sale)」与「有票(on_sale)」各一条，导致前端出现重复场次。
          // 这里按"可购票优先"去重：同一 key 只保留售票状态优先级最高的一条。
          java.util.LinkedHashMap<String, com.example.backend.response.app.TheaterHallShowTime> uniqueShows =
              new java.util.LinkedHashMap<>();
          for (var st : dateGroup.getData()) {
            String dedupKey = st.getTheaterHallId() + "|" + st.getStartTime() + "|" + st.getEndTime()
                + "|" + st.getDimensionType() + "|" + st.getVersionCode() + "|" + st.getSpecName();
            var existing = uniqueShows.get(dedupKey);
            if (existing == null
                || saleStatusRank(st.getSaleStatus()) < saleStatusRank(existing.getSaleStatus())) {
              uniqueShows.put(dedupKey, st);
            }
          }
          dateGroup.setData(new ArrayList<>(uniqueShows.values()));

          // 按照开始时间排序
          dateGroup.getData().sort((t1, t2) -> {
            String startTime1 = t1.getStartTime();
            String startTime2 = t2.getStartTime();
            if (startTime1 == null && startTime2 == null) return 0;
            if (startTime1 == null) return 1;
            if (startTime2 == null) return -1;
            return startTime1.compareTo(startTime2);
          });

          if (list.getCinemaId() != null && dateGroup.getDate() != null) {
            dateGroup.setServiceDays(
                cinemaServiceDayService.listForDate(list.getCinemaId(), dateGroup.getDate()));
          }
        }
      }
    }

    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /**
   * 同一物理场次去重时的售票状态优先级：返回值越小越优先保留。
   * 可购票状态（on_sale/few/无状态）优先于不可购票状态（预售/售罄/停售/关闭/未知），
   * 使前端在重复落库时展示可操作的那一条。
   */
  private static int saleStatusRank(String status) {
    if (status == null) return 2;
    switch (status.trim()) {
      case "on_sale": return 0;
      case "few": return 1;
      case "": return 2;
      case "sold_out": return 3;
      case "pre_sale": return 4;
      case "sale_ended": return 5;
      case "closed": return 6;
      default: return 7;
    }
  }

  @GetMapping(ApiPaths.Common.Cinema.SCREENING)
  public RestBean<Object> screening (@RequestParam("id") Integer id, @RequestParam("date") String date,
                                     @RequestParam(value = "use30HourFormat", required = false) Boolean use30HourFormat) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    QueryWrapper<TheaterHall> queryWrapper = new QueryWrapper<>();
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

    // 30 小时制（营业日）：电影院把 0:00~5:59 的午夜场算作「前一天」的营业日尾巴。
    // 默认按自然日（24h）返回；开启 use30HourFormat 时，以 6:00 为界重组成营业日 D：
    //   - 保留当天 >=6:00 的场次（含晚间跨午夜的，结束落在次日）；
    //   - 剔除当天 <6:00 的早场（归 D-1 营业日，会出现在 D-1 那一页）；
    //   - 并入次日 <6:00 的早场作为午夜尾巴（其 start_time 是次日 00:xx，前端按与本日零点
    //     的时间差自然落在 24:00-29:59）。
    if (use30HourFormat != null && use30HourFormat) {
      String nextDay = currentDate.plusDays(1).format(formatter);
      MovieShowTimeListQuery nextDayQuery = new MovieShowTimeListQuery();
      nextDayQuery.setDate(nextDay);
      nextDayQuery.setCinemaId(id);
      List<MovieShowTimeList> nextDayList =
        movieShowTimeMapper.movieShowTimeList(nextDayQuery, OrderState.order_succeed.getCode());

      List<MovieShowTimeList> businessDayList = new ArrayList<>();
      for (MovieShowTimeList item : movieShowTimeListList) {
        if (startHourOf(item) >= 6) businessDayList.add(item);
      }
      for (MovieShowTimeList item : nextDayList) {
        if (startHourOf(item) < 6) businessDayList.add(item);
      }
      movieShowTimeListList = businessDayList;
    }

    // 组装返回结果（lambda 捕获需 effectively final，故复制到 final 引用）
    final List<MovieShowTimeList> screeningList = movieShowTimeListList;
    List<CinemaScreeningResponse> result = theaterHallList.stream().map(item -> {
      CinemaScreeningResponse cinemaScreeningResponse = new CinemaScreeningResponse();

      cinemaScreeningResponse.setId(item.getId());
      cinemaScreeningResponse.setName(item.getName());
      cinemaScreeningResponse.setDate(today);


      List<MovieShowTimeList> screening = screeningList
        .stream()
        .map(movie -> {
          var showTimeTags = movieShowTimeMapper.getMovieShowTimeTags(movie.getMovieShowTimeTagsId());
          TagI18nUtils.translateShowTimeTags(showTimeTags);
          movie.setMovieShowTimeTags(showTimeTags);
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


    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }

  /**
   * 解析场次开场小时（0~23）。start_time 形如 "yyyy-MM-dd HH:mm:ss"，取第 11~12 位。
   * 无法解析时返回 6，使其按「白天场」保留，避免误丢数据。
   */
  private int startHourOf(MovieShowTimeList item) {
    String startTime = item.getStartTime();
    if (startTime == null || startTime.length() < 13) return 6;
    try {
      return Integer.parseInt(startTime.substring(11, 13));
    } catch (NumberFormatException e) {
      return 6;
    }
  }

  // 获取影院上映中的电影
  @GetMapping(ApiPaths.Common.Cinema.MOVIE_SHOWING)
  public RestBean<Object> GetMovieShowing(@RequestParam("id") Integer id) {
    List<MovieShowingResponse> result = cinemaMapper.getMovieShowing(id);
    if (!result.isEmpty()) {
      List<Integer> movieIds = result.stream().map(MovieShowingResponse::getId).toList();
      java.util.Map<Integer, Boolean> hasBenefitsMap = benefitService.hasBenefitsForMovies(movieIds, LocalDate.now().toString());
      result.forEach(m -> m.setHasBenefits(Boolean.TRUE.equals(hasBenefitsMap.get(m.getId()))));
    }
    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
  }
  @Transactional
  public void saveCinema(SaveCinemaQuery query) {
    Cinema cinema = new Cinema();
    Cinema before = query.getId() == null ? null : cinemaMapper.selectById(query.getId());

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
    lockCinemaFields(cinema.getId(), before, query);

    if (query.getSpec() != null) {
      List<CinemaSpecSpec> spec = query.getSpec().stream().map(item -> {
        CinemaSpecSpec modal = new CinemaSpecSpec();

        modal.setCinemaId(cinema.getId());
        modal.setSpecId(item.getSpecId());
        modal.setPlusPrice(item.getPlusPrice());

        return modal;
      }).toList();

      QueryWrapper<CinemaSpecSpec> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("cinema_id", cinema.getId());
      cinemaSpecSpecService.remove(queryWrapper);
      cinemaSpecSpecService.saveBatch(spec);
    }

    if (query.getTicketType() != null) {
      QueryWrapper<MovieTicketType> ttQw = new QueryWrapper<>();
      ttQw.eq("cinema_id", cinema.getId());
      movieTicketTypeMapper.delete(ttQw);
      int orderNum = 0;
      for (TicketTypeItem item : query.getTicketType()) {
        if (item.getName() != null && !item.getName().isEmpty() && item.getPrice() != null) {
          MovieTicketType tt = new MovieTicketType();
          tt.setCinemaId(cinema.getId());
          tt.setName(item.getName());
          tt.setPrice(BigDecimal.valueOf(item.getPrice()));
          tt.setDescription(item.getDescription());
          tt.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
          tt.setScheduleType(item.getScheduleType());
          tt.setApplicableWeekdays(toApplicableWeekdaysList(item.getApplicableWeekdays()));
          tt.setApplicableMonthDays(toApplicableMonthDaysList(item.getApplicableMonthDays()));
          tt.setApplicableDates(toApplicableDatesList(item.getApplicableDates()));
          tt.setDailyStartTime(item.getDailyStartTime());
          tt.setDailyEndTime(item.getDailyEndTime());
          tt.setOrderNum(orderNum++);
          movieTicketTypeMapper.insert(tt);
        }
      }
    }

    if (query.getPriceConfig() != null) {
      cinemaPriceConfigMapper.physicalDeleteByCinemaId(cinema.getId());
      for (PriceConfigItem item : query.getPriceConfig()) {
        if (item.getDimensionType() != null && item.getSurcharge() != null) {
          CinemaPriceConfig pc = new CinemaPriceConfig();
          pc.setCinemaId(cinema.getId());
          pc.setDimensionType(item.getDimensionType());
          pc.setSurcharge(item.getSurcharge());
          cinemaPriceConfigMapper.insert(pc);
        }
      }
    }
  }

  private void lockCinemaFields(Integer cinemaId, Cinema before, SaveCinemaQuery query) {
    if (cinemaId == null) return;

    List<String> fields = new ArrayList<>();
    ManualFieldLockUtils.addIfChanged(fields, "name", before == null ? null : before.getName(), query.getName());
    ManualFieldLockUtils.addIfChanged(fields, "address", before == null ? null : before.getAddress(), query.getAddress());
    ManualFieldLockUtils.addIfChanged(fields, "home_page", before == null ? null : before.getHomePage(), query.getHomePage());
    ManualFieldLockUtils.addIfChanged(fields, "tel", before == null ? null : before.getTel(), query.getTel());
    ManualFieldLockUtils.addIfChanged(fields, "description", before == null ? null : before.getDescription(), query.getDescription());
    ManualFieldLockUtils.addIfChanged(fields, "max_select_seat_count", before == null ? null : before.getMaxSelectSeatCount(), query.getMaxSelectSeatCount());
    ManualFieldLockUtils.addIfChanged(fields, "brand_id", before == null ? null : before.getBrandId(), query.getBrandId());
    ManualFieldLockUtils.addIfChanged(fields, "region_id", before == null ? null : before.getRegionId(), query.getRegionId());
    ManualFieldLockUtils.addIfChanged(fields, "prefecture_id", before == null ? null : before.getPrefectureId(), query.getPrefectureId());
    ManualFieldLockUtils.addIfChanged(fields, "city_id", before == null ? null : before.getCityId(), query.getCityId());
    ManualFieldLockUtils.addIfChanged(fields, "full_address", before == null ? null : before.getFullAddress(), query.getFullAddress());

    if (!fields.isEmpty()) {
      cinemaMapper.addManualLockedFields(cinemaId, fields);
    }
  }

  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @Transactional
  @PostMapping(ApiPaths.Admin.Cinema.SAVE)
  public RestBean<String> save(@RequestBody @Validated() SaveCinemaQuery query) {
    if (query.getId() == null) {
      QueryWrapper<Cinema> wrapper = new QueryWrapper<>();
      wrapper.eq("name", query.getName());
      List<Cinema> list = cinemaMapper.selectList(wrapper);

      if (list.size() == 0) {
        saveCinema(query);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
      } else {
        return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.REPEAT_ERROR));
      }
    } else {
      UpdateWrapper<Cinema> updateQueryWrapper = new UpdateWrapper<>();
      updateQueryWrapper.eq("id", query.getId());
      Cinema old = cinemaMapper.selectById(query.getId());

      if (Objects.equals(old.getName(), query.getName())) {
        saveCinema(query);
        return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
      } else {
        QueryWrapper<Cinema> wrapper = new QueryWrapper<>();
        wrapper.eq("name", query.getName());
        Cinema find = cinemaMapper.selectOne(wrapper);

        if (find != null) {
          return RestBean.error(ResponseCode.REPEAT.getCode(), MessageUtils.getMessage(MessageKeys.Admin.REPEAT_ERROR));
        } else {
          saveCinema(query);
          return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        }
      }
    }
  }
  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @Transactional
  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_SAVE)
  public RestBean<MovieTicketType> saveTicketTypes(@RequestBody @Validated CinemaTicketTypeSaveQuery query) {
    if (query.getCinemaId() == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    if (cinemaMapper.selectById(query.getCinemaId()) == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    TicketTypeItem item = query.getTicketType();
    if (item == null || item.getName() == null || item.getName().isEmpty() || item.getPrice() == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    MovieTicketType tt = new MovieTicketType();
    tt.setCinemaId(query.getCinemaId());
    tt.setName(item.getName());
    tt.setPrice(BigDecimal.valueOf(item.getPrice()));
    tt.setDescription(item.getDescription());
    tt.setEnabled(item.getEnabled() != null ? item.getEnabled() : true);
    tt.setScheduleType(item.getScheduleType());
    tt.setApplicableWeekdays(toApplicableWeekdaysList(item.getApplicableWeekdays()));
    tt.setApplicableMonthDays(toApplicableMonthDaysList(item.getApplicableMonthDays()));
    tt.setApplicableDates(toApplicableDatesList(item.getApplicableDates()));
    tt.setDailyStartTime(item.getDailyStartTime());
    tt.setDailyEndTime(item.getDailyEndTime());
    if (item.getId() != null) {
      MovieTicketType existing = movieTicketTypeMapper.selectById(item.getId());
      if (existing == null || !existing.getCinemaId().equals(query.getCinemaId())) {
        return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
      }
      tt.setId(item.getId());
      tt.setOrderNum(existing.getOrderNum());
      movieTicketTypeMapper.updateById(tt);
    } else {
      Integer maxOrder = movieTicketTypeMapper.selectMaxOrderNumByCinemaId(query.getCinemaId());
      tt.setOrderNum(maxOrder != null ? maxOrder + 1 : 0);
      movieTicketTypeMapper.insert(tt);
    }
    return RestBean.success(movieTicketTypeMapper.selectById(tt.getId()), MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @DeleteMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_REMOVE)
  public RestBean<String> removeTicketType(@RequestParam Integer id) {
    if (id == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    MovieTicketType tt = movieTicketTypeMapper.selectById(id);
    if (tt == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    movieTicketTypeMapper.deleteById(id);
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "cinema.save")
  @PostMapping(ApiPaths.Admin.Cinema.TICKET_TYPE_REORDER)
  public RestBean<String> reorderTicketTypes(@RequestBody CinemaTicketTypeReorderQuery reorderQuery) {
    if (reorderQuery.getCinemaId() == null || reorderQuery.getTicketTypeIds() == null || reorderQuery.getTicketTypeIds().isEmpty()) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    if (cinemaMapper.selectById(reorderQuery.getCinemaId()) == null) {
      return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));
    }
    for (int i = 0; i < reorderQuery.getTicketTypeIds().size(); i++) {
      Integer tid = reorderQuery.getTicketTypeIds().get(i);
      MovieTicketType tt = movieTicketTypeMapper.selectById(tid);
      if (tt != null && tt.getCinemaId().equals(reorderQuery.getCinemaId())) {
        tt.setOrderNum(i);
        movieTicketTypeMapper.updateById(tt);
      }
    }
    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.SAVE_SUCCESS));
  }

  @SaCheckLogin
  @CheckPermission(code = "cinema.remove")
  @DeleteMapping(ApiPaths.Admin.Cinema.REMOVE)
  public RestBean<Null> remove (@RequestParam Integer id) {
    if(id == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

    cinemaMapper.deleteById(id);

    return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
  }
  /** 将前端传入的 applicableWeekdays（"1,2,3" 或 [1,2,3]）转为 List<Integer> */
  private static List<Integer> toApplicableWeekdaysList(Object o) {
    return toIntegerList(o);
  }

  /** 将前端传入的 applicableMonthDays（[1,15,28] 或 "1,15,28"）转为 List<Integer> */
  private static List<Integer> toApplicableMonthDaysList(Object o) {
    return toIntegerList(o);
  }

  private static List<Integer> toIntegerList(Object o) {
    if (o == null) return null;
    if (o instanceof List) {
      List<?> list = (List<?>) o;
      return list.stream()
          .map(e -> e instanceof Number ? ((Number) e).intValue() : Integer.parseInt(String.valueOf(e)))
          .collect(Collectors.toList());
    }
    if (o instanceof String) {
      String s = ((String) o).trim();
      if (s.isEmpty()) return null;
      return Arrays.stream(s.split(","))
          .map(String::trim)
          .filter(x -> !x.isEmpty())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
    }
    return null;
  }

  /** 将前端传入的 applicableDates（["2025-01-01","2025-01-15"] 或 "2025-01-01,2025-01-15"）转为 List<String> */
  private static List<String> toApplicableDatesList(Object o) {
    if (o == null) return null;
    if (o instanceof List) {
      List<?> list = (List<?>) o;
      return list.stream().map(String::valueOf).filter(s -> !s.isBlank()).collect(Collectors.toList());
    }
    if (o instanceof String) {
      String s = ((String) o).trim();
      if (s.isEmpty()) return null;
      return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.toList());
    }
    return null;
  }

  @GetMapping(ApiPaths.Common.Cinema.SPEC)
  public RestBean<List<com.example.backend.response.Spec>> cinemaSpec (@RequestParam Integer cinemaId) {
      if(cinemaId == null) return RestBean.error(ResponseCode.PARAMETER_ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Admin.PARAMETER_ERROR));

      List<com.example.backend.response.Spec> result = cinemaMapper.getCinemaSpec(cinemaId);

      return RestBean.success(result, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
    }
}
