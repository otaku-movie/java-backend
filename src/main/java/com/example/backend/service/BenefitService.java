package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Benefit;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.exception.BusinessException;
import com.example.backend.entity.BenefitTheaterStock;
import com.example.backend.entity.BenefitUserFeedback;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.CinemaSpec;
import com.example.backend.entity.Movie;
import com.example.backend.mapper.BenefitMapper;
import com.example.backend.mapper.BenefitTheaterStockMapper;
import com.example.backend.mapper.BenefitUserFeedbackMapper;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.SpecMapper;
import com.example.backend.constants.MessageKeys;
import com.example.backend.utils.MessageUtils;
import com.example.backend.query.benefit.BenefitCinemaAvailabilityQuery;
import com.example.backend.query.benefit.BenefitFeedbackListQuery;
import com.example.backend.query.benefit.BenefitListQuery;
import com.example.backend.query.benefit.BenefitMovieListQuery;
import com.example.backend.query.benefit.BenefitStockListQuery;
import com.example.backend.query.benefit.BenefitStockSaveQuery;
import com.example.backend.response.benefit.BenefitCinemaAvailabilityItemResponse;
import com.example.backend.response.benefit.BenefitCinemaAvailabilityRow;
import com.example.backend.response.benefit.BenefitDetailResponse;
import com.example.backend.response.benefit.BenefitFeedbackListItemResponse;
import com.example.backend.response.benefit.BenefitListItemResponse;
import com.example.backend.response.benefit.BenefitMovieListItemResponse;
import com.example.backend.response.benefit.BenefitStockListItemResponse;
import com.example.backend.response.benefit.CinemaBenefitItemSummary;
import com.example.backend.response.benefit.CinemaBenefitSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson2.JSON;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入场者特典：阶段、物料、影院库存；判断电影/场次是否有特典；管理端 CRUD。
 */
@Service
public class BenefitService {

  @Autowired
  private BenefitMapper benefitMapper;
  @Autowired
  private BenefitTheaterStockMapper benefitTheaterStockMapper;
  @Autowired
  private BenefitUserFeedbackMapper benefitUserFeedbackMapper;
  @Autowired
  private MovieMapper movieMapper;
  @Autowired
  private CinemaMapper cinemaMapper;
  @Autowired
  private SpecMapper specMapper;
  @Autowired
  private BenefitFeedbackRedisService benefitFeedbackRedisService;
  @Autowired
  private BenefitFeedbackArchiver benefitFeedbackArchiver;

  @Value("${benefit.feedback.window-hours:24}")
  private int benefitFeedbackWindowHours;

  /**
   * 某电影在指定日期是否有有效特典（存在有效阶段）。
   */
  public boolean hasBenefitsForMovie(Integer movieId, Integer reReleaseId, String dateStr) {
    if (movieId == null || dateStr == null) return false;
    return !listBenefitsByMovieAndDate(movieId, reReleaseId, dateStr).isEmpty();
  }

  /**
   * 批量：多部电影是否有特典（仅查 benefit 表是否有记录，不校验日期和物料）。
   */
  public Map<Integer, Boolean> hasAnyBenefitsForMovies(Collection<Integer> movieIds) {
    Map<Integer, Boolean> out = new HashMap<>();
    if (movieIds == null || movieIds.isEmpty()) return out;
    for (Integer id : movieIds) out.put(id, false);
    // 仅普通上映特典（re_release_id 为空）参与该批量判断
    List<Benefit> all = benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>()
        .in(Benefit::getMovieId, movieIds)
        .isNull(Benefit::getReReleaseId));
    for (Benefit b : all) out.put(b.getMovieId(), true);
    return out;
  }

  /** 批量：重映条目是否有特典。返回 key(movieId_reReleaseId) -> 是否有特典 */
  public Map<String, Boolean> hasAnyBenefitsForReReleases(Collection<Integer> movieIds, Collection<Integer> reReleaseIds) {
    Map<String, Boolean> out = new HashMap<>();
    if (movieIds == null || movieIds.isEmpty() || reReleaseIds == null || reReleaseIds.isEmpty()) return out;
    List<Benefit> all = benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>()
        .in(Benefit::getMovieId, movieIds)
        .in(Benefit::getReReleaseId, reReleaseIds));
    for (Benefit b : all) {
      if (b.getMovieId() == null || b.getReReleaseId() == null) continue;
      out.put(b.getMovieId() + "_" + b.getReReleaseId(), true);
    }
    return out;
  }

  /**
   * 批量：多部电影在指定日期是否有特典。返回 movieId -> 是否有特典。
   */
  public Map<Integer, Boolean> hasBenefitsForMovies(Collection<Integer> movieIds, String dateStr) {
    Map<Integer, Boolean> out = new HashMap<>();
    if (movieIds == null || movieIds.isEmpty() || dateStr == null) return out;
    for (Integer id : movieIds) out.put(id, false);
    for (Integer movieId : movieIds) {
      if (!listBenefitsByMovieAndDate(movieId, null, dateStr).isEmpty()) {
        out.put(movieId, true);
      }
    }
    return out;
  }

  /**
   * 某场次是否有特典：电影在该场次日期有有效阶段，且该影院对某阶段有库存（remaining > 0）。
   */
  public boolean hasBenefitsForShowtime(Integer movieId, Integer cinemaId, String showDateStr,
                                       Integer reReleaseId,
                                       Integer dimensionType, List<Integer> specIds) {
    if (movieId == null || cinemaId == null || showDateStr == null) return false;
    List<Benefit> benefits = listBenefitsByMovieAndDate(movieId, reReleaseId, showDateStr);
    for (Benefit b : benefits) {
      BenefitTheaterStock stock = benefitTheaterStockMapper.selectOne(
        new LambdaQueryWrapper<BenefitTheaterStock>()
          .eq(BenefitTheaterStock::getCinemaId, cinemaId)
          .eq(BenefitTheaterStock::getBenefitId, b.getId()));
      if (stock != null && (stock.getRemaining() == null || stock.getRemaining() > 0)) {
        return true;
      }
    }
    return false;
  }

  public List<Benefit> listBenefitsByMovieAndDate(Integer movieId, Integer reReleaseId, String dateStr) {
    if (movieId == null || dateStr == null) return Collections.emptyList();
    LambdaQueryWrapper<Benefit> w = new LambdaQueryWrapper<Benefit>()
      .eq(Benefit::getMovieId, movieId)
      .le(Benefit::getStartDate, dateStr)
      .and(x -> x.isNull(Benefit::getEndDate).or().eq(Benefit::getEndDate, "").or().ge(Benefit::getEndDate, dateStr))
      .orderByAsc(Benefit::getOrderNum);
    if (reReleaseId == null) w.isNull(Benefit::getReReleaseId);
    else w.eq(Benefit::getReReleaseId, reReleaseId);
    return benefitMapper.selectList(w);
  }

  public Integer getStockRemaining(Integer cinemaId, Integer benefitId) {
    if (cinemaId == null || benefitId == null) return null;
    BenefitTheaterStock stock = benefitTheaterStockMapper.selectOne(
      new LambdaQueryWrapper<BenefitTheaterStock>()
        .eq(BenefitTheaterStock::getCinemaId, cinemaId)
        .eq(BenefitTheaterStock::getBenefitId, benefitId));
    return stock != null ? stock.getRemaining() : null;
  }

  // ==================== 管理端 ====================

  public IPage<BenefitListItemResponse> listBenefitForAdmin(BenefitListQuery query) {
    Page<Benefit> page = new Page<>(query.getPage() != null ? query.getPage() : 1,
      query.getPageSize() != null ? query.getPageSize() : 10);
    LambdaQueryWrapper<Benefit> wrapper = new LambdaQueryWrapper<>();
    if (query.getMovieId() != null) wrapper.eq(Benefit::getMovieId, query.getMovieId());
    if (query.getReReleaseId() != null) wrapper.eq(Benefit::getReReleaseId, query.getReReleaseId());
    if (query.getReReleaseId() == null) wrapper.isNull(Benefit::getReReleaseId);
    if (StringUtils.hasText(query.getName())) wrapper.like(Benefit::getName, query.getName());
    wrapper.orderByAsc(Benefit::getOrderNum).orderByDesc(Benefit::getId);
    IPage<Benefit> result = benefitMapper.selectPage(page, wrapper);
    List<Integer> movieIds = result.getRecords().stream().map(Benefit::getMovieId).distinct().toList();
    Map<Integer, String> movieNameMap = new HashMap<>();
    Map<Integer, String> movieCoverMap = new HashMap<>();
    for (Integer mid : movieIds) {
      Movie m = movieMapper.selectById(mid);
      movieNameMap.put(mid, m != null ? m.getName() : null);
      movieCoverMap.put(mid, m != null && StringUtils.hasText(m.getCover()) ? m.getCover() : null);
    }
    List<Integer> benefitIds = result.getRecords().stream().map(Benefit::getId).toList();
    Map<Integer, Integer> benefitRemainingMap = new HashMap<>();
    Map<Integer, Boolean> benefitRemainingUnknown = new HashMap<>();
    if (!benefitIds.isEmpty()) {
      List<BenefitTheaterStock> stocks = benefitTheaterStockMapper.selectList(
        new LambdaQueryWrapper<BenefitTheaterStock>().in(BenefitTheaterStock::getBenefitId, benefitIds));
      for (BenefitTheaterStock s : stocks) {
        Integer bid = s.getBenefitId();
        if (bid == null) continue;
        if (s.getRemaining() == null) {
          benefitRemainingUnknown.put(bid, true);
        } else {
          benefitRemainingMap.merge(bid, s.getRemaining(), Integer::sum);
        }
      }
    }
    List<BenefitListItemResponse> list = result.getRecords().stream().map(b -> {
      BenefitListItemResponse r = new BenefitListItemResponse();
      r.setId(b.getId());
      r.setMovieId(b.getMovieId());
      r.setReReleaseId(b.getReReleaseId());
      r.setMovieName(movieNameMap.get(b.getMovieId()));
      r.setMovieCover(movieCoverMap.get(b.getMovieId()));
      r.setName(b.getName());
      r.setQuantity(b.getQuantity());
      r.setDescription(b.getDescription());
      r.setImageUrls(parseStringList(b.getImageUrls()));
      r.setStartDate(b.getStartDate());
      r.setEndDate(StringUtils.hasText(b.getEndDate()) ? b.getEndDate() : null);
      r.setOrderNum(b.getOrderNum());
      r.setItemCount(0);
      Integer remaining = Boolean.TRUE.equals(benefitRemainingUnknown.get(b.getId())) ? null : benefitRemainingMap.get(b.getId());
      if (remaining == null) remaining = b.getRemaining();
      r.setRemaining(remaining);
      r.setStatus(b.getPhaseStatus() != null ? b.getPhaseStatus() : computePhaseStatus(b.getStartDate(), b.getEndDate()));
      return r;
    }).toList();
    Page<BenefitListItemResponse> out = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
    out.setRecords(list);
    return out;
  }

  public IPage<BenefitMovieListItemResponse> listBenefitMoviesForAdmin(BenefitMovieListQuery query) {
    if (query == null) query = new BenefitMovieListQuery();
    Page<BenefitMovieListItemResponse> page = new Page<>(
      query.getPage() != null ? query.getPage() : 1,
      query.getPageSize() != null ? query.getPageSize() : 10
    );
    return benefitMapper.listMoviesWithBenefit(page, query);
  }

  /** App 端：按电影 ID 获取该电影下所有特典阶段详情（含物料列表） */
  /** App 端：按开始时间倒序（新的在前） */
  public List<BenefitDetailResponse> listBenefitDetailByMovie(Integer movieId, Integer reReleaseId) {
    if (movieId == null) return Collections.emptyList();
    LambdaQueryWrapper<Benefit> w = new LambdaQueryWrapper<Benefit>()
      .eq(Benefit::getMovieId, movieId);
    if (reReleaseId == null) w.isNull(Benefit::getReReleaseId);
    else w.eq(Benefit::getReReleaseId, reReleaseId);
    w.orderByDesc(Benefit::getStartDate).orderByDesc(Benefit::getId);
    List<Benefit> benefits = benefitMapper.selectList(w);
    return benefits.stream().map(b -> getBenefitDetail(b.getId())).filter(Objects::nonNull).toList();
  }

  public BenefitDetailResponse getBenefitDetail(Integer id) {
    if (id == null) return null;
    Benefit b = benefitMapper.selectById(id);
    if (b == null) return null;
    BenefitDetailResponse r = new BenefitDetailResponse();
    r.setId(b.getId());
    r.setMovieId(b.getMovieId());
    r.setReReleaseId(b.getReReleaseId());
    Movie m = movieMapper.selectById(b.getMovieId());
    r.setMovieName(m != null ? m.getName() : null);
    r.setName(b.getName());
    r.setQuantity(b.getQuantity());
    r.setDescription(b.getDescription());
    r.setImageUrls(parseStringList(b.getImageUrls()));
    r.setDimensionType(b.getDimensionType());
    List<Integer> sidList = parseIntegerList(b.getSpecIds());
    r.setSpecIds(sidList);
    if (!sidList.isEmpty()) {
      r.setSpecNames(sidList.stream()
        .map(specMapper::selectById)
        .filter(Objects::nonNull)
        .map(CinemaSpec::getName)
        .collect(Collectors.toList()));
    }
    r.setCinemaLimitType(b.getCinemaLimitType() != null ? b.getCinemaLimitType() : 0);
    r.setCinemaIds(parseIntegerList(b.getCinemaIds()));
    r.setStartDate(b.getStartDate());
    r.setEndDate(StringUtils.hasText(b.getEndDate()) ? b.getEndDate() : null);
    r.setRemaining(b.getRemaining());
    r.setOrderNum(b.getOrderNum());
    r.setStatus(b.getPhaseStatus() != null ? b.getPhaseStatus() : computePhaseStatus(b.getStartDate(), b.getEndDate()));
    r.setAvailableCinemaCount(benefitTheaterStockMapper.countAvailableCinemasForBenefit(b.getId()));
    return r;
  }

  /**
   * App：按特典分页查询影院库存与反馈聚合。
   */
  public IPage<BenefitCinemaAvailabilityItemResponse> pageCinemasForBenefitApp(
    Integer benefitId,
    Integer reReleaseId,
    Integer regionId,
    Integer prefectureId,
    String keyword,
    String sort,
    Double latitude,
    Double longitude,
    int page,
    int pageSize,
    Integer currentUserId
  ) {
    if (benefitId == null) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Admin.PARAMETER_ERROR);
    }
    Benefit b = benefitMapper.selectById(benefitId);
    if (b == null) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Admin.PARAMETER_ERROR);
    }
    if (b.getReReleaseId() == null) {
      if (reReleaseId != null) {
        throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Admin.PARAMETER_ERROR);
      }
    } else {
      if (!Objects.equals(reReleaseId, b.getReReleaseId())) {
        throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Admin.PARAMETER_ERROR);
      }
    }

    String sortNorm = StringUtils.hasText(sort) ? sort.trim() : "remainingDesc";
    if ("distance".equalsIgnoreCase(sortNorm) && (latitude == null || longitude == null)) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Admin.PARAMETER_ERROR);
    }

    BenefitCinemaAvailabilityQuery q = new BenefitCinemaAvailabilityQuery();
    q.setBenefitId(benefitId);
    q.setMovieId(b.getMovieId());
    q.setReReleaseId(b.getReReleaseId());
    q.setRegionId(regionId);
    q.setPrefectureId(prefectureId);
    q.setKeyword(StringUtils.hasText(keyword) ? keyword.trim() : null);
    q.setSort(sortNorm);
    q.setLatitude(latitude);
    q.setLongitude(longitude);

    int limitType = b.getCinemaLimitType() != null ? b.getCinemaLimitType() : 0;
    List<Integer> whitelist = parseIntegerList(b.getCinemaIds());
    if (limitType == 1) {
      q.setWhitelistEnabled(true);
      q.setWhitelistIds(whitelist);
      if (whitelist.isEmpty()) {
        Page<BenefitCinemaAvailabilityItemResponse> empty = new Page<>(page, pageSize);
        empty.setTotal(0);
        empty.setRecords(Collections.emptyList());
        return empty;
      }
    } else {
      q.setWhitelistEnabled(false);
      q.setWhitelistIds(null);
    }

    long total = benefitTheaterStockMapper.countAvailability(q);
    long offset = (long) (page - 1) * pageSize;
    List<BenefitCinemaAvailabilityRow> rows = benefitTheaterStockMapper.listAvailability(q, pageSize, offset);

    List<BenefitCinemaAvailabilityItemResponse> items = new ArrayList<>();
    for (BenefitCinemaAvailabilityRow row : rows) {
      BenefitCinemaAvailabilityItemResponse it = new BenefitCinemaAvailabilityItemResponse();
      it.setCinemaId(row.getCinemaId());
      it.setCinemaName(row.getCinemaName());
      it.setBrandName(row.getBrandName());
      it.setFullAddress(row.getFullAddress());
      it.setRegionId(row.getRegionId());
      it.setPrefectureId(row.getPrefectureId());
      it.setLatitude(row.getLatitude());
      it.setLongitude(row.getLongitude());
      it.setQuota(row.getQuota());
      it.setRemaining(row.getRemaining());
      if (row.getDistanceMeters() != null) {
        it.setDistanceKm(row.getDistanceMeters() / 1000.0);
      }
      int cid = row.getCinemaId() != null ? row.getCinemaId() : 0;
      boolean soldFb = benefitFeedbackRedisService.isSoldOutByFeedback(benefitId, cid);
      int fbCount = benefitFeedbackRedisService.getFeedbackCount(benefitId, cid);
      int manual = row.getManualSoldOut() != null ? row.getManualSoldOut() : 0;
      it.setStockStatus(computeBenefitStockDisplayStatus(manual, row.getRemaining(), row.getQuota(), soldFb));
      it.setFeedbackCount(fbCount);
      it.setFeedbackWindowHours(benefitFeedbackWindowHours);
      if (currentUserId != null && cid > 0) {
        it.setCurrentUserFeedbackSubmitted(hasUserSubmittedFeedback(currentUserId, cid, benefitId));
      } else {
        it.setCurrentUserFeedbackSubmitted(false);
      }
      it.setShowTimeCount(row.getShowTimeCount());
      it.setNearestShowTime(row.getNearestShowTime());
      items.add(it);
    }

    Page<BenefitCinemaAvailabilityItemResponse> out = new Page<>(page, pageSize, total);
    out.setRecords(items);
    return out;
  }

  public void resetBenefitFeedbackCache(Integer benefitId, Integer cinemaId) {
    if (benefitId == null || cinemaId == null) return;
    benefitFeedbackRedisService.resetKeys(benefitId, cinemaId);
  }

  /** 字典 benefitStockStatus：1充足 2少量 3极少 4已领完 5未知 6用户反馈领完 */
  private static int computeBenefitStockDisplayStatus(int manualSoldOut, Integer remaining, Integer quota, boolean soldOutByFeedback) {
    // 运营手动 / 用户反馈领完：权威「已领完」，与剩余数无关
    if (manualSoldOut == 1) return 4;
    if (soldOutByFeedback) return 6;
    int q = quota != null && quota > 0 ? quota : 0;
    // 剩余未上报：未知
    if (remaining == null) return 5;
    if (remaining <= 0) {
      // 没有有效配额（库存从未被维护）时，剩余 0 仅为占位，按「未知」展示而非「已领完」；
      // 仅当配额 > 0 且剩余减到 0 才是真正「已领完」。
      return q <= 0 ? 5 : 4;
    }
    if (q <= 0) return 1;
    double ratio = remaining / (double) q;
    if (ratio >= 0.3) return 1;
    if (ratio >= 0.1) return 2;
    return 3;
  }

  /** 按开始/结束日期计算阶段状态：1=之前 2=进行中 3=已结束（字典 benefitPhaseStatus） */
  private static Integer computePhaseStatus(String startDate, String endDate) {
    LocalDate today = LocalDate.now();
    if (StringUtils.hasText(endDate)) {
      try {
        if (today.isAfter(LocalDate.parse(endDate))) return 3;
      } catch (DateTimeParseException ignored) { }
    }
    if (StringUtils.hasText(startDate)) {
      try {
        if (today.isBefore(LocalDate.parse(startDate))) return 1;
      } catch (DateTimeParseException ignored) { }
    }
    return 2;
  }

  private static List<String> parseStringList(String json) {
    if (json == null || json.isBlank()) return Collections.emptyList();
    try {
      List<String> list = JSON.parseArray(json, String.class);
      return list != null ? list : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  /** 校验日期格式 yyyy-MM-dd */
  private static boolean isValidDate(String s) {
    if (s == null || s.isBlank()) return false;
    try {
      LocalDate.parse(s);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  private static List<Integer> parseIntegerList(String json) {
    if (json == null || json.isBlank()) return Collections.emptyList();
    try {
      List<Integer> list = JSON.parseArray(json, Integer.class);
      return list != null ? list : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public Integer saveBenefit(com.example.backend.query.benefit.BenefitSaveQuery query) {
    Benefit b = query.getId() != null ? benefitMapper.selectById(query.getId()) : new Benefit();
    if (b == null) b = new Benefit();
    b.setMovieId(query.getMovieId());
    b.setReReleaseId(query.getReReleaseId());
    b.setName(query.getName());
    b.setQuantity(query.getQuantity());
    b.setDescription(query.getDescription());
    b.setImageUrls(query.getImageUrls() != null && !query.getImageUrls().isEmpty()
      ? JSON.toJSONString(query.getImageUrls()) : null);
    b.setDimensionType(query.getDimensionType());
    b.setSpecIds(query.getSpecIds() != null && !query.getSpecIds().isEmpty()
      ? JSON.toJSONString(query.getSpecIds()) : null);
    boolean hasCinemaIds = query.getCinemaIds() != null && !query.getCinemaIds().isEmpty();
    int cinemaLimitType = query.getCinemaLimitType() != null ? query.getCinemaLimitType() : (hasCinemaIds ? 1 : 0);
    b.setCinemaLimitType(cinemaLimitType);
    b.setCinemaIds(hasCinemaIds ? JSON.toJSONString(query.getCinemaIds()) : null);
    String startDate = StringUtils.hasText(query.getStartDate()) ? query.getStartDate().trim() : null;
    String endDate = StringUtils.hasText(query.getEndDate()) ? query.getEndDate().trim() : null;
    if (!isValidDate(startDate)) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.BENEFIT_DATE_INVALID);
    }
    if (endDate != null && !isValidDate(endDate)) {
      throw new BusinessException(ResponseCode.PARAMETER_ERROR, MessageKeys.Error.BENEFIT_DATE_INVALID);
    }
    b.setStartDate(startDate);
    b.setEndDate(endDate);
    b.setRemaining(query.getRemaining());
    b.setOrderNum(query.getOrderNum() != null ? query.getOrderNum() : 0);
    b.setPhaseStatus(computePhaseStatus(startDate, endDate));
    if (b.getId() == null) {
      benefitMapper.insert(b);
    } else {
      benefitMapper.updateById(b);
    }
    return b.getId();
  }

  /**
   * 仅更新某特典的「影院限定」，不动其它字段。
   * cinemaIds 为空 => 不限定（cinemaLimitType=0）；非空 => 限定（cinemaLimitType=1）。
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateBenefitCinemaLimit(com.example.backend.query.benefit.BenefitCinemaLimitSaveQuery query) {
    if (query.getBenefitId() == null) {
      throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_ITEM_REQUIRED));
    }
    Benefit b = benefitMapper.selectById(query.getBenefitId());
    if (b == null) {
      throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_STOCK_NOT_FOUND));
    }
    boolean hasCinemaIds = query.getCinemaIds() != null && !query.getCinemaIds().isEmpty();
    b.setCinemaLimitType(hasCinemaIds ? 1 : 0);
    b.setCinemaIds(hasCinemaIds ? JSON.toJSONString(query.getCinemaIds()) : null);
    benefitMapper.updateById(b);
  }

  @Transactional(rollbackFor = Exception.class)
  public void removeBenefit(Integer id) {
    if (id == null) return;
    benefitMapper.deleteById(id);
  }

  public IPage<BenefitStockListItemResponse> listStockForAdmin(BenefitStockListQuery query) {
    Page<BenefitTheaterStock> page = new Page<>(query.getPage() != null ? query.getPage() : 1,
      query.getPageSize() != null ? query.getPageSize() : 10);
    LambdaQueryWrapper<BenefitTheaterStock> wrapper = new LambdaQueryWrapper<>();
    if (query.getCinemaId() != null) wrapper.eq(BenefitTheaterStock::getCinemaId, query.getCinemaId());
    if (query.getBenefitId() != null) wrapper.eq(BenefitTheaterStock::getBenefitId, query.getBenefitId());
    if (StringUtils.hasText(query.getCinemaName())) {
      List<Integer> matchedCinemaIds = cinemaMapper.selectList(
        new LambdaQueryWrapper<Cinema>().like(Cinema::getName, query.getCinemaName().trim()))
        .stream().map(Cinema::getId).toList();
      if (matchedCinemaIds.isEmpty()) {
        return new Page<>(page.getCurrent(), page.getSize(), 0);
      }
      wrapper.in(BenefitTheaterStock::getCinemaId, matchedCinemaIds);
    }
    wrapper.orderByDesc(BenefitTheaterStock::getId);
    IPage<BenefitTheaterStock> result = benefitTheaterStockMapper.selectPage(page, wrapper);
    List<BenefitStockListItemResponse> list = result.getRecords().stream().map(stock -> {
      BenefitStockListItemResponse r = new BenefitStockListItemResponse();
      r.setId(stock.getId());
      r.setCinemaId(stock.getCinemaId());
      Cinema c = cinemaMapper.selectById(stock.getCinemaId());
      r.setCinemaName(c != null ? c.getName() : null);
      r.setBenefitId(stock.getBenefitId());
      Benefit benefit = benefitMapper.selectById(stock.getBenefitId());
      r.setBenefitName(benefit != null ? benefit.getName() : null);
      r.setQuota(stock.getQuota());
      r.setRemaining(stock.getRemaining());
      r.setManualSoldOut(stock.getManualSoldOut() != null ? stock.getManualSoldOut() : 0);
      return r;
    }).toList();
    Page<BenefitStockListItemResponse> out = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
    out.setRecords(list);
    return out;
  }

  @Transactional(rollbackFor = Exception.class)
  public void saveBenefitStock(BenefitStockSaveQuery query) {
    BenefitTheaterStock stock;
    if (query.getId() != null) {
      stock = benefitTheaterStockMapper.selectById(query.getId());
      if (stock == null) {
        throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_STOCK_NOT_FOUND));
      }
    } else {
      if (query.getBenefitId() == null) {
        throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_ITEM_REQUIRED));
      }
      if (query.getCinemaId() == null) {
        throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Validator.BenefitStock.CINEMA_ID_REQUIRED));
      }
      // 影院限定校验：该特典若设置了影院限定（cinemaLimitType=1），只能给白名单内的影院分配库存；
      // 限定为空（不限定）则不校验。需要新增范围外影院时，应先到阶段编辑的「影院限定」中添加。
      Benefit limitBenefit = benefitMapper.selectById(query.getBenefitId());
      if (limitBenefit != null
        && limitBenefit.getCinemaLimitType() != null
        && limitBenefit.getCinemaLimitType() == 1) {
        List<Integer> whitelist = parseIntegerList(limitBenefit.getCinemaIds());
        if (!whitelist.isEmpty() && !whitelist.contains(query.getCinemaId())) {
          throw new IllegalArgumentException(
            MessageUtils.getMessage(MessageKeys.Error.BENEFIT_STOCK_CINEMA_NOT_IN_LIMIT));
        }
      }
      stock = benefitTheaterStockMapper.selectOne(
        new LambdaQueryWrapper<BenefitTheaterStock>()
          .eq(BenefitTheaterStock::getCinemaId, query.getCinemaId())
          .eq(BenefitTheaterStock::getBenefitId, query.getBenefitId()));
      if (stock != null) {
        // 该影院已分配过：视为「追加分配」，在原有配额/剩余基础上累加，不影响已领走的部分。
        if (query.getQuota() != null) {
          int baseQuota = stock.getQuota() != null ? stock.getQuota() : 0;
          stock.setQuota(baseQuota + query.getQuota());
        }
        Integer addRemaining = query.getRemaining() != null ? query.getRemaining() : query.getQuota();
        if (addRemaining != null) {
          int baseRemaining = stock.getRemaining() != null ? stock.getRemaining() : 0;
          stock.setRemaining(baseRemaining + addRemaining);
        }
        if (query.getManualSoldOut() != null) stock.setManualSoldOut(query.getManualSoldOut());
        benefitTheaterStockMapper.updateById(stock);
        return;
      }
    }
    if (stock == null) {
      stock = new BenefitTheaterStock();
      stock.setCinemaId(query.getCinemaId());
      stock.setBenefitId(query.getBenefitId());
      stock.setQuota(query.getQuota());
      stock.setRemaining(query.getRemaining() != null ? query.getRemaining() : query.getQuota());
      stock.setManualSoldOut(query.getManualSoldOut() != null ? query.getManualSoldOut() : 0);
      benefitTheaterStockMapper.insert(stock);
    } else {
      if (query.getQuota() != null) stock.setQuota(query.getQuota());
      if (query.getRemaining() != null) stock.setRemaining(query.getRemaining());
      if (query.getManualSoldOut() != null) stock.setManualSoldOut(query.getManualSoldOut());
      benefitTheaterStockMapper.updateById(stock);
    }
  }

  /**
   * 管理端 - 按电影查询影院特典汇总：每家影院有多少（配额）、剩余多少、用户反馈数。
   */
  public List<CinemaBenefitSummaryResponse> listCinemaBenefitSummaryByMovie(Integer movieId) {
    if (movieId == null) return Collections.emptyList();
    List<Benefit> benefits = benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>().eq(Benefit::getMovieId, movieId));
    if (benefits.isEmpty()) return Collections.emptyList();
    List<Integer> benefitIds = benefits.stream().map(Benefit::getId).toList();
    Map<Integer, Benefit> benefitMap = benefits.stream().collect(Collectors.toMap(Benefit::getId, b -> b));
    List<BenefitTheaterStock> stocks = benefitTheaterStockMapper.selectList(
      new LambdaQueryWrapper<BenefitTheaterStock>().in(BenefitTheaterStock::getBenefitId, benefitIds));
    Set<Integer> cinemaIds = stocks.stream().map(BenefitTheaterStock::getCinemaId).collect(Collectors.toSet());
    Map<Integer, String> cinemaNameMap = new HashMap<>();
    for (Integer cid : cinemaIds) {
      Cinema c = cinemaMapper.selectById(cid);
      cinemaNameMap.put(cid, c != null ? c.getName() : null);
    }
    Map<Integer, Long> feedbackCountMap = new HashMap<>();
    if (!cinemaIds.isEmpty()) {
      List<BenefitUserFeedback> feedbacks = benefitUserFeedbackMapper.selectList(
        new LambdaQueryWrapper<BenefitUserFeedback>()
          .in(BenefitUserFeedback::getCinemaId, cinemaIds)
          .in(BenefitUserFeedback::getBenefitId, benefitIds));
      for (BenefitUserFeedback f : feedbacks) {
        feedbackCountMap.merge(f.getCinemaId(), 1L, Long::sum);
      }
    }
    Map<Integer, List<BenefitTheaterStock>> byCinema = stocks.stream().collect(Collectors.groupingBy(BenefitTheaterStock::getCinemaId));
    List<CinemaBenefitSummaryResponse> result = new ArrayList<>();
    for (Integer cinemaId : cinemaIds) {
      CinemaBenefitSummaryResponse r = new CinemaBenefitSummaryResponse();
      r.setCinemaId(cinemaId);
      r.setCinemaName(cinemaNameMap.get(cinemaId));
      List<BenefitTheaterStock> cinemaStocks = byCinema.getOrDefault(cinemaId, Collections.emptyList());
      List<CinemaBenefitItemSummary> itemSummaries = new ArrayList<>();
      int totalQuota = 0;
      Integer totalRemaining = 0;
      boolean hasNullRemaining = false;
      for (BenefitTheaterStock s : cinemaStocks) {
        CinemaBenefitItemSummary is = new CinemaBenefitItemSummary();
        Benefit b = benefitMap.get(s.getBenefitId());
        is.setBenefitId(s.getBenefitId());
        is.setBenefitName(b != null ? b.getName() : null);
        is.setQuota(s.getQuota());
        is.setRemaining(s.getRemaining());
        itemSummaries.add(is);
        if (s.getQuota() != null) totalQuota += s.getQuota();
        if (s.getRemaining() != null) totalRemaining += s.getRemaining();
        else hasNullRemaining = true;
      }
      r.setItems(itemSummaries);
      r.setTotalQuota(totalQuota);
      r.setTotalRemaining(hasNullRemaining ? null : totalRemaining);
      r.setFeedbackCount(feedbackCountMap.getOrDefault(cinemaId, 0L).intValue());
      result.add(r);
    }
    result.sort(Comparator.comparing(r -> r.getCinemaName() != null ? r.getCinemaName() : ""));
    return result;
  }

  /**
   * 判断当前用户是否已对「某影院 + 某特典」提交过反馈（用于订单详情是否展示反馈入口）。
   */
  public boolean hasUserSubmittedFeedback(Integer userId, Integer cinemaId, Integer benefitId) {
    if (userId == null || cinemaId == null || benefitId == null) return false;
    if (benefitFeedbackRedisService.hasUserSubmitted(benefitId, cinemaId, userId)) return true;
    long count = benefitUserFeedbackMapper.selectCount(
      new LambdaQueryWrapper<BenefitUserFeedback>()
        .eq(BenefitUserFeedback::getUserId, userId)
        .eq(BenefitUserFeedback::getCinemaId, cinemaId)
        .eq(BenefitUserFeedback::getBenefitId, benefitId));
    return count > 0;
  }

  /**
   * 订单对应场次特典：取该电影在该场次日期下的第一个有效阶段 ID（与 App 端「特典反馈」选第一个阶段一致）。
   */
  public Integer getFirstBenefitIdForOrder(Integer movieId, String dateStr) {
    if (movieId == null || dateStr == null) return null;
    List<Benefit> list = listBenefitsByMovieAndDate(movieId, null, dateStr);
    return list.isEmpty() ? null : list.get(0).getId();
  }

  /**
   * 用户提交特典反馈（如：该影院该阶段已领完）。需登录，userId 由调用方传入。
   */
  public void submitFeedback(Integer userId, Integer cinemaId, Integer benefitId, Integer feedbackType) {
    if (cinemaId == null || benefitId == null) return;
    if (feedbackType == null) feedbackType = 1;
    int uid = userId != null ? userId : 0;
    benefitFeedbackRedisService.recordFeedback(benefitId, cinemaId, uid);
    BenefitUserFeedback f = new BenefitUserFeedback();
    f.setUserId(uid);
    f.setCinemaId(cinemaId);
    f.setBenefitId(benefitId);
    f.setFeedbackType(feedbackType);
    benefitFeedbackArchiver.archive(f);
  }

  public IPage<BenefitFeedbackListItemResponse> listFeedbackForAdmin(BenefitFeedbackListQuery query) {
    Page<BenefitUserFeedback> page = new Page<>(query.getPage() != null ? query.getPage() : 1,
      query.getPageSize() != null ? query.getPageSize() : 10);
    LambdaQueryWrapper<BenefitUserFeedback> wrapper = new LambdaQueryWrapper<>();
    if (query.getCinemaId() != null) wrapper.eq(BenefitUserFeedback::getCinemaId, query.getCinemaId());
    if (query.getBenefitId() != null) wrapper.eq(BenefitUserFeedback::getBenefitId, query.getBenefitId());
    if (query.getFeedbackType() != null) wrapper.eq(BenefitUserFeedback::getFeedbackType, query.getFeedbackType());
    if (query.getIsRead() != null) wrapper.eq(BenefitUserFeedback::getIsRead, query.getIsRead());
    wrapper.orderByDesc(BenefitUserFeedback::getId);
    IPage<BenefitUserFeedback> result = benefitUserFeedbackMapper.selectPage(page, wrapper);
    Set<Integer> cinemaIds = result.getRecords().stream().map(BenefitUserFeedback::getCinemaId).collect(Collectors.toSet());
    Set<Integer> benefitIds = result.getRecords().stream().map(BenefitUserFeedback::getBenefitId).collect(Collectors.toSet());
    Map<Integer, String> cinemaNameMap = new HashMap<>();
    for (Integer cid : cinemaIds) {
      Cinema c = cinemaMapper.selectById(cid);
      cinemaNameMap.put(cid, c != null ? c.getName() : null);
    }
    Map<Integer, String> benefitNameMap = new HashMap<>();
    for (Integer bid : benefitIds) {
      Benefit b = benefitMapper.selectById(bid);
      benefitNameMap.put(bid, b != null ? b.getName() : null);
    }
    List<BenefitFeedbackListItemResponse> list = result.getRecords().stream().map(f -> {
      BenefitFeedbackListItemResponse r = new BenefitFeedbackListItemResponse();
      r.setId(f.getId());
      r.setUserId(f.getUserId());
      r.setCinemaId(f.getCinemaId());
      r.setCinemaName(cinemaNameMap.get(f.getCinemaId()));
      r.setBenefitId(f.getBenefitId());
      r.setBenefitName(benefitNameMap.get(f.getBenefitId()));
      r.setFeedbackType(f.getFeedbackType());
      r.setIsRead(f.getIsRead());
      r.setCreateTime(f.getCreateTime());
      return r;
    }).toList();
    Page<BenefitFeedbackListItemResponse> out = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
    out.setRecords(list);
    return out;
  }

  /**
   * 后台标记反馈为已读：按特典 / 影院维度，把未读（is_read=0）批量置为已读。
   * benefitId 与 cinemaId 至少传一个，二者都传则取交集。
   *
   * @return 本次置为已读的条数
   */
  public int markFeedbackRead(Integer benefitId, Integer cinemaId) {
    if (benefitId == null && cinemaId == null) return 0;
    LambdaUpdateWrapper<BenefitUserFeedback> wrapper = new LambdaUpdateWrapper<>();
    if (benefitId != null) wrapper.eq(BenefitUserFeedback::getBenefitId, benefitId);
    if (cinemaId != null) wrapper.eq(BenefitUserFeedback::getCinemaId, cinemaId);
    wrapper.eq(BenefitUserFeedback::getIsRead, 0);
    wrapper.set(BenefitUserFeedback::getIsRead, 1);
    return benefitUserFeedbackMapper.update(null, wrapper);
  }
}
