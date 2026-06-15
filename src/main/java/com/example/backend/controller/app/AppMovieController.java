package com.example.backend.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ShowTimeState;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.ReReleaseMapper;
import com.example.backend.query.app.AppMovieListQuery;
import com.example.backend.service.BenefitService;
import com.example.backend.query.app.getMovieShowTimeQuery;
import com.example.backend.response.app.*;
import com.example.backend.utils.MessageUtils;
import com.example.backend.utils.Utils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



@Data
class getMovieStaffQuery {
  private Integer movieId;
}

@RestController
public class AppMovieController {
  @Autowired
  private MovieMapper movieMapper;
  @Autowired
  private ReReleaseMapper reReleaseMapper;
  @Autowired
  private BenefitService benefitService;
  @Autowired
  private com.example.backend.service.FavoriteCinemaService favoriteCinemaService;

  @GetMapping(ApiPaths.App.Movie.NOW_SHOWING)
  public RestBean<List<NowMovieShowingResponse>> list(
    @ModelAttribute AppMovieListQuery query
  )  {
    Page<MovieMapper> page = new Page<>(query.getPage(), query.getPageSize());

    // 第一步：获取基本的电影信息（普通上映）
    IPage<NowMovieShowingResponse> list = movieMapper.nowMovieShowing(query, page);

    // 追加：合并当前日期有效的重映电影（仅合并到第 1 页，避免复杂分页）
    if (query.getPage() != null && query.getPage() == 1) {
      String today = LocalDate.now().toString();
      List<NowMovieShowingResponse> reReleases = reReleaseMapper.activeNowShowing(today);
      // 有搜索关键字时，重映条目也需按片名过滤，避免搜索结果混入无关重映
      String kw = query.getName() == null ? "" : query.getName().trim();
      if (!kw.isEmpty() && reReleases != null) {
        String kwLower = kw.toLowerCase();
        reReleases = reReleases.stream()
          .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(kwLower))
          .toList();
      }
      if (reReleases != null && !reReleases.isEmpty()) {
        // 不再按 movieId 去重：重映条目需要在列表里可见（即使该电影也在普通上映中）
        List<NowMovieShowingResponse> merged = new ArrayList<>();
        merged.addAll(reReleases);
        if (list.getRecords() != null) merged.addAll(list.getRecords());
        // 截断到 pageSize
        if (query.getPageSize() != null && merged.size() > query.getPageSize()) {
          merged = merged.subList(0, query.getPageSize());
        }
        list.setRecords(merged);
        list.setTotal(list.getTotal() + reReleases.size());
      }
    }
    
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

      // 第五步.5：批量获取監督（监督）并按 movieId 分组塞回
      List<com.example.backend.response.app.MovieDirectorRow> directorRows =
          movieMapper.getDirectorsByMovieIds(movieIds);
      Map<Integer, List<com.example.backend.response.Staff>> directorMap = directorRows.stream()
          .collect(Collectors.groupingBy(
              com.example.backend.response.app.MovieDirectorRow::getMovieId,
              Collectors.mapping(row -> {
                com.example.backend.response.Staff s = new com.example.backend.response.Staff();
                s.setId(row.getId());
                s.setName(row.getName());
                s.setCover(row.getCover());
                return s;
              }, Collectors.toList())
          ));
      list.getRecords().forEach(movie ->
          movie.setDirector(directorMap.getOrDefault(movie.getId(), Collections.emptyList()))
      );

      // 第五步.55：批量获取出演演员并按 movieId 分组塞回
      List<com.example.backend.response.app.MovieCastRow> castRows =
          movieMapper.getCastByMovieIds(movieIds);
      Map<Integer, List<com.example.backend.response.Staff>> castMap = castRows.stream()
          .collect(Collectors.groupingBy(
              com.example.backend.response.app.MovieCastRow::getMovieId,
              Collectors.mapping(row -> {
                com.example.backend.response.Staff s = new com.example.backend.response.Staff();
                s.setId(row.getId());
                s.setName(row.getName());
                s.setCover(row.getCover());
                return s;
              }, Collectors.toList())
          ));
      list.getRecords().forEach(movie ->
          movie.setCast(castMap.getOrDefault(movie.getId(), Collections.emptyList()))
      );

      // 第五步.6：批量获取上映规格（IMAX/4DX 等）并按 movieId 分组塞回
      List<com.example.backend.response.app.MovieSpecRow> specRows =
          movieMapper.getSpecsByMovieIds(movieIds);
      Map<Integer, List<com.example.backend.response.Spec>> specMap = specRows.stream()
          .collect(Collectors.groupingBy(
              com.example.backend.response.app.MovieSpecRow::getMovieId,
              Collectors.mapping(row -> {
                com.example.backend.response.Spec s = new com.example.backend.response.Spec();
                s.setId(row.getId());
                s.setName(row.getName());
                s.setDescription(row.getDescription());
                return s;
              }, Collectors.toList())
          ));
      list.getRecords().forEach(movie ->
          movie.setSpec(specMap.getOrDefault(movie.getId(), Collections.emptyList()))
      );

      // 普通上映/重映的特典需要区分：普通上映只看 re_release_id 为空；重映看对应 re_release_id
      List<Integer> normalMovieIds = list.getRecords().stream()
        .filter(m -> m.getIsReRelease() == null || !m.getIsReRelease() || m.getReReleaseId() == null)
        .map(NowMovieShowingResponse::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
      Map<Integer, Boolean> normalBenefitsMap = benefitService.hasAnyBenefitsForMovies(normalMovieIds);

      List<NowMovieShowingResponse> rrRecords = list.getRecords().stream()
        .filter(m -> Boolean.TRUE.equals(m.getIsReRelease()) && m.getReReleaseId() != null && m.getId() != null)
        .toList();
      List<Integer> rrMovieIds = rrRecords.stream().map(NowMovieShowingResponse::getId).distinct().toList();
      List<Integer> rrIds = rrRecords.stream().map(NowMovieShowingResponse::getReReleaseId).distinct().toList();
      Map<String, Boolean> rrBenefitsMap = benefitService.hasAnyBenefitsForReReleases(rrMovieIds, rrIds);

      list.getRecords().forEach(movie -> {
        if (Boolean.TRUE.equals(movie.getIsReRelease()) && movie.getReReleaseId() != null && movie.getId() != null) {
          movie.setHasBenefits(Boolean.TRUE.equals(rrBenefitsMap.get(movie.getId() + "_" + movie.getReReleaseId())));
        } else {
          movie.setHasBenefits(Boolean.TRUE.equals(normalBenefitsMap.get(movie.getId())));
        }
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

    // 合并未来重映（仅第 1 页）。与主列表「名字相同」的条目跳过，避免同一部电影
    // 出现两次且重映行可能带着院线小缩略图 cover。
    if (query.getPage() != null && query.getPage() == 1) {
      String today = LocalDate.now().toString();
      List<MovieComingSoonResponse> reReleases = reReleaseMapper.upcomingComingSoon(today);
      // 有搜索关键字时，重映条目也需按片名（含原名）过滤
      String kw = query.getName() == null ? "" : query.getName().trim();
      if (!kw.isEmpty() && reReleases != null) {
        String kwLower = kw.toLowerCase();
        reReleases = reReleases.stream()
          .filter(m -> (m.getName() != null && m.getName().toLowerCase().contains(kwLower))
            || (m.getOriginalName() != null && m.getOriginalName().toLowerCase().contains(kwLower)))
          .toList();
      }
      if (reReleases != null && !reReleases.isEmpty()) {
        Set<Integer> existingMovieIds = new HashSet<>();
        Set<String> existingNames = new HashSet<>();
        if (list.getRecords() != null) {
          for (MovieComingSoonResponse m : list.getRecords()) {
            if (m.getId() != null) {
              existingMovieIds.add(m.getId());
            }
            String norm = normalizeMovieNameKey(m.getName());
            if (norm != null) {
              existingNames.add(norm);
            }
          }
        }
        List<MovieComingSoonResponse> extraReleases = reReleases.stream()
            .filter(rr -> rr.getId() == null || !existingMovieIds.contains(rr.getId()))
            .filter(rr -> {
              String norm = normalizeMovieNameKey(rr.getName());
              return norm == null || !existingNames.contains(norm);
            })
            .collect(Collectors.toList());
        if (!extraReleases.isEmpty()) {
          List<MovieComingSoonResponse> merged = new ArrayList<>();
          merged.addAll(extraReleases);
          if (list.getRecords() != null) {
            merged.addAll(list.getRecords());
          }
          // 重映与主列表混在一起后必须按「上映日」重新整体排序，否则重映行会一直
          // 堆在最前，出现「上一组 7/3、下一组 6/4」这种乱序。排序键与主查询
          // getMovieComingSoon 的 ORDER BY 保持一致：空日期排最后，其余按
          // loose_release_to_date 折算的代表日升序（解析不出的按 9999 排最后）。
          merged.sort(Comparator
              .comparingInt((MovieComingSoonResponse m) ->
                  (m.getStartDate() == null || m.getStartDate().trim().isEmpty()) ? 1 : 0)
              .thenComparing(m -> looseReleaseSortKey(m.getStartDate())));
          // 不再截断回 pageSize：之前 subList(0, pageSize) 会把原本属于第 1 页末尾的
          // 正片挤掉，而第 2 页从 DB offset=pageSize 开始，被挤掉的那几部就永远不
          // 出现（中间缺一段）。这里让第 1 页带着这几条重映多出来即可，第 2 页照常
          // 衔接，不丢片。重复 id 由客户端按 id 去重。
          list.setRecords(merged);
          list.setTotal(list.getTotal() + extraReleases.size());
        }
      }
    }

    return RestBean.success(list.getRecords(), query.getPage(), list.getTotal(), query.getPageSize());
  }
//  获取电影的上映场次
  @PostMapping(ApiPaths.App.Movie.SHOW_TIME)
  public RestBean<Object> showTime (@RequestBody getMovieShowTimeQuery query) {
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

    // 收藏影院置顶：登录用户注入 userId，SQL 会按 is_favorite DESC 优先排序，
    // 进而保证「按影院分页」时收藏的影院排在最前。未登录则为 null，排序不变。
    query.setUserId(favoriteCinemaService.currentUserIdOrNull());

    // 注意：这里 *不* 把 IPage 传给 mapper —— 见 MovieMapper#getMovieShowTime 上的说明。
    // 详情页"场次列表"是按影院分页（一页 = N 家影院的所有场次），而 mybatis-plus
    // 的分页插件只能给 SQL 末尾追加 LIMIT N，会把所有名额塞给 cinema.id 最小的那家。
    List<AppBeforeMovieShowTimeResponse> list = movieMapper.getMovieShowTime(query, ShowTimeState.no_started.getCode());

    // 在做"按影院维度分页切片"之前，先用全量数据计算每个日期的 summary：
    //   - cinemaCount：当日去重的影院数
    //   - showTimeCount：当日的场次总数
    // 这样无论 app 端拉到第几页，顶部摘要条都能展示准确总数，而不是只统计已加载部分。
    final boolean use30HourForSummary =
        query.getUse30HourFormat() != null && query.getUse30HourFormat();
    Map<String, Set<Integer>> dateCinemaIds = new LinkedHashMap<>();
    Map<String, Integer> dateShowTimeCount = new LinkedHashMap<>();
    for (AppBeforeMovieShowTimeResponse item : list) {
      String startTime = item.getStartTime();
      if (startTime == null || startTime.length() < 10) {
        continue;
      }
      String dateKey;
      if (use30HourForSummary) {
        // 30 小时制下 02:00 之类的次日凌晨场归在前一天 26:00；用同一份转换函数保证
        // 与 result list 的 date 一致，避免摘要与 tab 对不上。
        String timeToConvert = startTime.length() >= 16 ? startTime.substring(0, 16) : startTime;
        String converted = Utils.convert24HourTo30Hour(timeToConvert);
        if (converted == null || converted.length() < 10) continue;
        dateKey = converted.substring(0, 10);
      } else {
        dateKey = startTime.substring(0, 10);
      }
      if (item.getCinemaId() != null) {
        dateCinemaIds.computeIfAbsent(dateKey, k -> new HashSet<>()).add(item.getCinemaId());
      }
      dateShowTimeCount.put(dateKey, dateShowTimeCount.getOrDefault(dateKey, 0) + 1);
    }
    List<Map<String, Object>> summary = new ArrayList<>();
    List<String> summaryDates = new ArrayList<>(dateShowTimeCount.keySet());
    Collections.sort(summaryDates); // yyyy-MM-dd 字典序即日期升序
    for (String date : summaryDates) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("date", date);
      row.put("cinemaCount",
          dateCinemaIds.getOrDefault(date, Collections.emptySet()).size());
      row.put("showTimeCount", dateShowTimeCount.get(date));
      summary.add(row);
    }

    // 按影院维度分页：保留 mapper 返回顺序（cinema.id ASC，或距离 ASC——已由 SQL ORDER BY 决定），
    // 取出前 pageSize 个 cinema_id，再过滤 list 只保留这些影院的场次。
    int pageNum = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
    LinkedHashSet<Integer> orderedCinemaIds = list.stream()
        .map(AppBeforeMovieShowTimeResponse::getCinemaId)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<Integer> pagedCinemaIdSet = orderedCinemaIds.stream()
        .skip((long) (pageNum - 1) * pageSize)
        .limit(pageSize)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    list = list.stream()
        .filter(item -> item.getCinemaId() != null && pagedCinemaIdSet.contains(item.getCinemaId()))
        .collect(Collectors.toList());

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
      // 用 LinkedHashMap 保留 mapper 的 SQL 顺序（is_favorite DESC, distance ASC）；
      // 默认 groupingBy 返回 HashMap 会打乱顺序，导致收藏影院无法置顶。
      Map<Integer, List<AppBeforeMovieShowTimeResponse>> cinema = map.get(key).stream().collect(
          Collectors.groupingBy(
              AppBeforeMovieShowTimeResponse::getCinemaId,
              LinkedHashMap::new,
              Collectors.toList()));

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
          showTime.setSpecNames(item.getSpecName() != null && !item.getSpecName().isEmpty()
              ? Arrays.asList(item.getSpecName().split("、"))
              : new ArrayList<>());
          // 放映类型为空时默认 1（2D）
          showTime.setDimensionType(item.getDimensionType());
          showTime.setTotalSeats(item.getTotalSeats());
          showTime.setSelectedSeats(item.getSelectedSeats());
          // 计算可用座位数
          Integer availableSeats = item.getTotalSeats() - item.getSelectedSeats();
          showTime.setAvailableSeats(availableSeats);
          showTime.setMovieVersionId(item.getMovieVersionId());
          showTime.setVersionCode(item.getVersionCode());
          showTime.setReReleaseId(item.getReReleaseId());
          showTime.setReReleaseVersionInfo(item.getReReleaseVersionInfo());
          showTime.setReservationUrl(item.getReservationUrl());
          showTime.setSaleStatus(item.getSaleStatus());
          showTime.setEventTitle(item.getEventTitle());
          // 字幕语言：mapper 已按 subtitle_id 原顺序「、」拼好；这里 split 成 List 供前端 chip 直接渲染
          showTime.setSubtitleNames(
              item.getSubtitleNames() != null && !item.getSubtitleNames().isEmpty()
                  ? Arrays.asList(item.getSubtitleNames().split("、"))
                  : new ArrayList<>());
          String showDateStr = item.getStartTime() != null && item.getStartTime().length() >= 10
              ? item.getStartTime().substring(0, 10) : null;
          List<Integer> specIds = item.getSpecIds() != null ? item.getSpecIds() : Collections.emptyList();
          showTime.setHasBenefits(benefitService.hasBenefitsForShowtime(
              item.getMovieId(), item.getCinemaId(), showDateStr, item.getReReleaseId(), item.getDimensionType(), specIds));
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
        model.setFavorite(Boolean.TRUE.equals(first.getFavorite()));  // 是否已收藏
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
    // 用 wrapper 同时返回分页后的场次列表（data）和全量 perDate 摘要（summary）。
    // 前端顶部"X 家影院 · Y 场"用 summary 来展示准确总数，不受分页影响。
    Map<String, Object> wrapper = new LinkedHashMap<>();
    wrapper.put("data", sorted);
    wrapper.put("summary", summary);
    return RestBean.success(wrapper, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  /** 电影详情页：重映历史（按 movieId 查询） */
  @GetMapping(ApiPaths.App.Movie.RE_RELEASE_HISTORY)
  public RestBean<List<ReReleaseHistoryResponse>> reReleaseHistory(@RequestParam Integer movieId) {
    List<ReReleaseHistoryResponse> list = reReleaseMapper.historyByMovieId(movieId);
    return RestBean.success(list, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  /**
   * 电影详情页"场次列表"的动态筛选项。
   *
   * 返回该电影（含 reReleaseId 区分）所有未来场次实际出现过的 distinct 字幕语言 id
   * 与上映标签 id；前端据此过滤本地全量字幕/标签字典，只展示这部电影真正有的选项。
   * 仅按 movieId / reReleaseId 限定，不随地区/规格/版本等其它筛选联动。
   */
  @PostMapping(ApiPaths.App.Movie.SHOW_TIME_FILTERS)
  public RestBean<Object> showTimeFilters(@RequestBody getMovieShowTimeQuery query) {
    Integer state = ShowTimeState.no_started.getCode();
    List<Integer> subtitleIds = movieMapper.getMovieShowTimeSubtitleIds(query, state);
    List<Integer> showTimeTagIds = movieMapper.getMovieShowTimeTagIds(query, state);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("subtitleIds", subtitleIds);
    result.put("showTimeTagIds", showTimeTagIds);
    return RestBean.success(result, MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS));
  }

  /** 片名去空白、小写，用于「即将上映」主列表与重映合并时的同名去重。 */
  private static String normalizeMovieNameKey(String name) {
    if (name == null) {
      return null;
    }
    String trimmed = name.trim();
    return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
  }

  /**
   * 把 start_date（可能是精确日期或日式模糊标签 `2026年秋` 等）折算成可排序的代表日，
   * 逻辑与 DB 函数 crawl.loose_release_to_date 对齐，用于合并重映后对整页统一排序。
   * 解析不出的（含 null/空）返回 LocalDate.MAX，对应 SQL 里的 9999-12-31（排最后）。
   */
  private static LocalDate looseReleaseSortKey(String startDate) {
    if (startDate == null) {
      return LocalDate.MAX;
    }
    String v = startDate.trim();
    if (v.isEmpty()) {
      return LocalDate.MAX;
    }
    try {
      // ISO / 斜杠：完整、年月、年
      if (v.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
        return LocalDate.parse(v.substring(0, 10));
      }
      Matcher isoYm = Pattern.compile("^(\\d{4})-(\\d{1,2})$").matcher(v);
      if (isoYm.matches()) {
        return LocalDate.of(Integer.parseInt(isoYm.group(1)), Integer.parseInt(isoYm.group(2)), 1);
      }
      Matcher slash = Pattern.compile("^(\\d{4})/(\\d{1,2})/(\\d{1,2})").matcher(v);
      if (slash.find()) {
        return LocalDate.of(Integer.parseInt(slash.group(1)),
            Integer.parseInt(slash.group(2)), Integer.parseInt(slash.group(3)));
      }
      Matcher slashYm = Pattern.compile("^(\\d{4})/(\\d{1,2})").matcher(v);
      if (slashYm.find()) {
        return LocalDate.of(Integer.parseInt(slashYm.group(1)),
            Integer.parseInt(slashYm.group(2)), 1);
      }
      if (v.matches("^\\d{4}$")) {
        return LocalDate.of(Integer.parseInt(v), 1, 1);
      }

      // 以下只处理 `YYYY年...` 的日式写法
      Matcher yearJp = Pattern.compile("^(\\d{4})年").matcher(v);
      if (!yearJp.find()) {
        return LocalDate.MAX;
      }
      int y = Integer.parseInt(yearJp.group(1));

      Matcher ymd = Pattern.compile("^\\d{4}年(\\d{1,2})月(\\d{1,2})日").matcher(v);
      if (ymd.find()) {
        return LocalDate.of(y, Integer.parseInt(ymd.group(1)), Integer.parseInt(ymd.group(2)));
      }
      Matcher ym = Pattern.compile("^\\d{4}年(\\d{1,2})月").matcher(v);
      if (ym.find()) {
        return LocalDate.of(y, Integer.parseInt(ym.group(1)), 1);
      }

      // YYYY年 + 季节/时期 → 代表月 1 日（顺序与 SQL 一致，先匹配更具体的）
      if (v.matches(".*(正月|年始|年明け).*")) return LocalDate.of(y, 1, 1);
      if (v.matches(".*(初春|早春).*")) return LocalDate.of(y, 2, 1);
      if (v.matches(".*(ゴールデンウィーク|ＧＷ|GW).*")) return LocalDate.of(y, 5, 1);
      if (v.contains("春")) return LocalDate.of(y, 4, 1);
      if (v.matches(".*(初夏|梅雨).*")) return LocalDate.of(y, 6, 1);
      if (v.contains("お盆")) return LocalDate.of(y, 8, 1);
      if (v.matches(".*(盛夏|真夏|夏).*")) return LocalDate.of(y, 7, 1);
      if (v.contains("初秋")) return LocalDate.of(y, 9, 1);
      if (v.contains("晩秋")) return LocalDate.of(y, 11, 1);
      if (v.contains("秋")) return LocalDate.of(y, 10, 1);
      if (v.matches(".*(初冬|年末).*")) return LocalDate.of(y, 12, 1);
      if (v.contains("冬")) return LocalDate.of(y, 12, 1);

      // 只有 YYYY年 → 当年 1/1
      return LocalDate.of(y, 1, 1);
    } catch (Exception e) {
      return LocalDate.MAX;
    }
  }

}
