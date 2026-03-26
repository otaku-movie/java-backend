package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.example.backend.query.benefit.BenefitFeedbackListQuery;
import com.example.backend.query.benefit.BenefitListQuery;
import com.example.backend.query.benefit.BenefitStockListQuery;
import com.example.backend.query.benefit.BenefitStockSaveQuery;
import com.example.backend.response.benefit.BenefitDetailResponse;
import com.example.backend.response.benefit.BenefitFeedbackListItemResponse;
import com.example.backend.response.benefit.BenefitListItemResponse;
import com.example.backend.response.benefit.BenefitStockListItemResponse;
import com.example.backend.response.benefit.CinemaBenefitItemSummary;
import com.example.backend.response.benefit.CinemaBenefitSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

  /**
   * 某电影在指定日期是否有有效特典（存在有效阶段）。
   */
  public boolean hasBenefitsForMovie(Integer movieId, String dateStr) {
    if (movieId == null || dateStr == null) return false;
    return !listBenefitsByMovieAndDate(movieId, dateStr).isEmpty();
  }

  /**
   * 批量：多部电影是否有特典（仅查 benefit 表是否有记录，不校验日期和物料）。
   */
  public Map<Integer, Boolean> hasAnyBenefitsForMovies(Collection<Integer> movieIds) {
    Map<Integer, Boolean> out = new HashMap<>();
    if (movieIds == null || movieIds.isEmpty()) return out;
    for (Integer id : movieIds) out.put(id, false);
    List<Benefit> all = benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>().in(Benefit::getMovieId, movieIds));
    for (Benefit b : all) out.put(b.getMovieId(), true);
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
      if (!listBenefitsByMovieAndDate(movieId, dateStr).isEmpty()) {
        out.put(movieId, true);
      }
    }
    return out;
  }

  /**
   * 某场次是否有特典：电影在该场次日期有有效阶段，且该影院对某阶段有库存（remaining > 0）。
   */
  public boolean hasBenefitsForShowtime(Integer movieId, Integer cinemaId, String showDateStr,
                                        Integer dimensionType, List<Integer> specIds) {
    if (movieId == null || cinemaId == null || showDateStr == null) return false;
    List<Benefit> benefits = listBenefitsByMovieAndDate(movieId, showDateStr);
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

  public List<Benefit> listBenefitsByMovieAndDate(Integer movieId, String dateStr) {
    if (movieId == null || dateStr == null) return Collections.emptyList();
    return benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>()
        .eq(Benefit::getMovieId, movieId)
        .le(Benefit::getStartDate, dateStr)
        .and(w -> w.isNull(Benefit::getEndDate).or().eq(Benefit::getEndDate, "").or().ge(Benefit::getEndDate, dateStr))
        .orderByAsc(Benefit::getOrderNum));
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
    if (StringUtils.hasText(query.getName())) wrapper.like(Benefit::getName, query.getName());
    wrapper.orderByAsc(Benefit::getOrderNum).orderByDesc(Benefit::getId);
    IPage<Benefit> result = benefitMapper.selectPage(page, wrapper);
    List<Integer> movieIds = result.getRecords().stream().map(Benefit::getMovieId).distinct().toList();
    Map<Integer, String> movieNameMap = new HashMap<>();
    for (Integer mid : movieIds) {
      Movie m = movieMapper.selectById(mid);
      movieNameMap.put(mid, m != null ? m.getName() : null);
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
      r.setMovieName(movieNameMap.get(b.getMovieId()));
      r.setName(b.getName());
      r.setQuantity(b.getQuantity());
      r.setDescription(b.getDescription());
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

  /** App 端：按电影 ID 获取该电影下所有特典阶段详情（含物料列表） */
  /** App 端：按开始时间倒序（新的在前） */
  public List<BenefitDetailResponse> listBenefitDetailByMovie(Integer movieId) {
    if (movieId == null) return Collections.emptyList();
    List<Benefit> benefits = benefitMapper.selectList(
      new LambdaQueryWrapper<Benefit>()
        .eq(Benefit::getMovieId, movieId)
        .orderByDesc(Benefit::getStartDate)
        .orderByDesc(Benefit::getId));
    return benefits.stream().map(b -> getBenefitDetail(b.getId())).filter(Objects::nonNull).toList();
  }

  public BenefitDetailResponse getBenefitDetail(Integer id) {
    if (id == null) return null;
    Benefit b = benefitMapper.selectById(id);
    if (b == null) return null;
    BenefitDetailResponse r = new BenefitDetailResponse();
    r.setId(b.getId());
    r.setMovieId(b.getMovieId());
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
    return r;
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
    } else {
      if (query.getBenefitId() == null) {
        throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_ITEM_REQUIRED));
      }
      stock = benefitTheaterStockMapper.selectOne(
        new LambdaQueryWrapper<BenefitTheaterStock>()
          .eq(BenefitTheaterStock::getCinemaId, query.getCinemaId())
          .eq(BenefitTheaterStock::getBenefitId, query.getBenefitId()));
      if (stock != null) {
        throw new IllegalArgumentException(MessageUtils.getMessage(MessageKeys.Error.BENEFIT_STOCK_DUPLICATE));
      }
    }
    if (stock == null) {
      stock = new BenefitTheaterStock();
      stock.setCinemaId(query.getCinemaId());
      stock.setBenefitId(query.getBenefitId());
      stock.setQuota(query.getQuota());
      stock.setRemaining(query.getRemaining() != null ? query.getRemaining() : query.getQuota());
      benefitTheaterStockMapper.insert(stock);
    } else {
      if (query.getQuota() != null) stock.setQuota(query.getQuota());
      if (query.getRemaining() != null) stock.setRemaining(query.getRemaining());
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
    List<Benefit> list = listBenefitsByMovieAndDate(movieId, dateStr);
    return list.isEmpty() ? null : list.get(0).getId();
  }

  /**
   * 用户提交特典反馈（如：该影院该阶段已领完）。需登录，userId 由调用方传入。
   */
  @Transactional(rollbackFor = Exception.class)
  public void submitFeedback(Integer userId, Integer cinemaId, Integer benefitId, Integer feedbackType) {
    if (cinemaId == null || benefitId == null) return;
    if (feedbackType == null) feedbackType = 1;
    BenefitUserFeedback f = new BenefitUserFeedback();
    f.setUserId(userId != null ? userId : 0);
    f.setCinemaId(cinemaId);
    f.setBenefitId(benefitId);
    f.setFeedbackType(feedbackType);
    benefitUserFeedbackMapper.insert(f);
  }

  public IPage<BenefitFeedbackListItemResponse> listFeedbackForAdmin(BenefitFeedbackListQuery query) {
    Page<BenefitUserFeedback> page = new Page<>(query.getPage() != null ? query.getPage() : 1,
      query.getPageSize() != null ? query.getPageSize() : 10);
    LambdaQueryWrapper<BenefitUserFeedback> wrapper = new LambdaQueryWrapper<>();
    if (query.getCinemaId() != null) wrapper.eq(BenefitUserFeedback::getCinemaId, query.getCinemaId());
    if (query.getBenefitId() != null) wrapper.eq(BenefitUserFeedback::getBenefitId, query.getBenefitId());
    if (query.getFeedbackType() != null) wrapper.eq(BenefitUserFeedback::getFeedbackType, query.getFeedbackType());
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
      r.setCreateTime(f.getCreateTime());
      return r;
    }).toList();
    Page<BenefitFeedbackListItemResponse> out = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
    out.setRecords(list);
    return out;
  }
}
