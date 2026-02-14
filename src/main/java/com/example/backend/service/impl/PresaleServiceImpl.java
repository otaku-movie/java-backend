package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Movie;
import com.example.backend.entity.Presale;
import com.example.backend.entity.PresaleSpecification;
import com.example.backend.mapper.MovieMapper;
import com.example.backend.mapper.PresaleMapper;
import com.example.backend.mapper.PresaleSpecificationMapper;
import com.example.backend.query.presale.PresaleListQuery;
import com.example.backend.query.presale.PresaleSaveQuery;
import com.example.backend.response.presale.PresaleDetailResponse;
import com.example.backend.response.presale.PresaleListItemResponse;
import com.example.backend.service.PresaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PresaleServiceImpl implements PresaleService {

  @Autowired
  private PresaleMapper presaleMapper;

  @Autowired
  private PresaleSpecificationMapper presaleSpecificationMapper;

  @Autowired
  private MovieMapper movieMapper;

  @Override
  public PresaleDetailResponse getDetail(Integer id) {
    if (id == null) {
      return null;
    }
    Presale presale = presaleMapper.selectById(id);
    if (presale == null) {
      return null;
    }
    PresaleDetailResponse response = toDetailResponse(presale);
    List<PresaleSpecification> specs = presaleSpecificationMapper.selectList(
      Wrappers.<PresaleSpecification>lambdaQuery()
        .eq(PresaleSpecification::getPresaleId, id)
        .orderByAsc(PresaleSpecification::getId)
    );
    response.setSpecifications(specs.stream().map(this::toSpecItem).collect(Collectors.toList()));
    return response;
  }

  @Override
  public IPage<PresaleListItemResponse> list(PresaleListQuery query) {
    Page<Presale> page = new Page<>(query.getPage() != null ? query.getPage() : 1, query.getPageSize() != null ? query.getPageSize() : 10);
    var wrapper = Wrappers.<Presale>lambdaQuery();
    if (StringUtils.hasText(query.getTitle())) {
      wrapper.like(Presale::getTitle, query.getTitle());
    }
    if (StringUtils.hasText(query.getCode())) {
      wrapper.eq(Presale::getCode, query.getCode());
    }
    if (query.getMovieId() != null) {
      wrapper.eq(Presale::getMovieId, query.getMovieId());
    }
    wrapper.orderByDesc(Presale::getUpdateTime).orderByDesc(Presale::getId);

    IPage<Presale> result = presaleMapper.selectPage(page, wrapper);
    List<Integer> presaleIds = result.getRecords().stream().map(Presale::getId).toList();
    Map<Integer, List<PresaleDetailResponse.SpecItem>> specMap = presaleIds.isEmpty()
      ? Map.of()
      : presaleSpecificationMapper.selectList(
          Wrappers.<PresaleSpecification>lambdaQuery()
            .in(PresaleSpecification::getPresaleId, presaleIds)
            .orderByAsc(PresaleSpecification::getId)
        ).stream()
        .collect(Collectors.groupingBy(PresaleSpecification::getPresaleId,
          Collectors.mapping(this::toSpecItem, Collectors.toList())));

    return result.convert(p -> {
      PresaleListItemResponse item = new PresaleListItemResponse();
      item.setId(p.getId());
      item.setCode(p.getCode());
      item.setTitle(p.getTitle());
      item.setDeliveryType(p.getDeliveryType());
      item.setDiscountMode(p.getDiscountMode());
      item.setPrice(p.getPrice());
      item.setTotalQuantity(p.getTotalQuantity());
      item.setLaunchTime(p.getLaunchTime());
      item.setEndTime(p.getEndTime());
      item.setUsageStart(p.getUsageStart());
      item.setUsageEnd(p.getUsageEnd());
      item.setPerUserLimit(p.getPerUserLimit() != null ? p.getPerUserLimit() : 1);
      item.setMovieId(p.getMovieId());
      if (p.getMovieId() != null) {
        Movie movie = movieMapper.selectById(p.getMovieId());
        if (movie != null && movie.getName() != null) {
          item.setMovieName(movie.getName());
        }
      }
      item.setCover(p.getCover());
      item.setGallery(p.getGallery());
      item.setSpecifications(specMap.getOrDefault(p.getId(), Collections.emptyList()));
      return item;
    });
  }

  @Override
  @Transactional
  public void save(PresaleSaveQuery query) {
    boolean isCreate = (query.getId() == null);

    // 校验：同一电影只能有一个预售票
    if (query.getMovieId() != null) {
      var existWrapper = Wrappers.<Presale>lambdaQuery()
          .eq(Presale::getMovieId, query.getMovieId());
      if (!isCreate) {
        existWrapper.ne(Presale::getId, query.getId());
      }
      long existCount = presaleMapper.selectCount(existWrapper);
      if (existCount > 0) {
        throw new IllegalArgumentException("该电影已存在预售票，每个电影只能关联一个预售票");
      }
    }

    Presale presale;

    if (isCreate) {
      presale = new Presale();
      presale.setCode(StringUtils.hasText(query.getCode()) ? query.getCode() : generateCode());
      presale.setTitle(query.getTitle());
      presale.setDeliveryType(query.getDeliveryType());
      presale.setDiscountMode(query.getDiscountMode() != null ? query.getDiscountMode() : 1);
      BigDecimal derivedPrice = derivePriceFromSpecs(query.getSpecifications());
      presale.setPrice(derivedPrice);
      presale.setAmount(query.getAmount() != null ? query.getAmount() : derivedPrice);
      presale.setTotalQuantity(0);
      presale.setLaunchTime(query.getLaunchTime());
      presale.setEndTime(query.getEndTime());
      presale.setUsageStart(query.getUsageStart());
      presale.setUsageEnd(query.getUsageEnd());
      presale.setPerUserLimit(query.getPerUserLimit() != null ? query.getPerUserLimit() : 1);
      presale.setMovieId(query.getMovieId());
      presale.setPickupNotes(query.getPickupNotes());
      presale.setCover(query.getCover());
      presale.setGallery(query.getGallery());
      presaleMapper.insert(presale);
    } else {
      presale = presaleMapper.selectById(query.getId());
      if (presale == null) {
        throw new IllegalArgumentException("Presale not found: " + query.getId());
      }
      if (StringUtils.hasText(query.getCode())) {
        presale.setCode(query.getCode());
      }
      presale.setTitle(query.getTitle());
      presale.setDeliveryType(query.getDeliveryType());
      presale.setDiscountMode(query.getDiscountMode() != null ? query.getDiscountMode() : 1);
      BigDecimal derivedPrice = derivePriceFromSpecs(query.getSpecifications());
      presale.setPrice(derivedPrice);
      presale.setAmount(query.getAmount() != null ? query.getAmount() : derivedPrice);
      presale.setTotalQuantity(0);
      presale.setLaunchTime(query.getLaunchTime());
      presale.setEndTime(query.getEndTime());
      presale.setUsageStart(query.getUsageStart());
      presale.setUsageEnd(query.getUsageEnd());
      presale.setPerUserLimit(query.getPerUserLimit() != null ? query.getPerUserLimit() : 1);
      presale.setMovieId(query.getMovieId());
      presale.setPickupNotes(query.getPickupNotes());
      presale.setCover(query.getCover());
      presale.setGallery(query.getGallery());
      presaleMapper.updateById(presale);

      presaleSpecificationMapper.delete(
        Wrappers.<PresaleSpecification>lambdaQuery().eq(PresaleSpecification::getPresaleId, presale.getId())
      );
    }

    Integer presaleId = presale.getId();
    if (!CollectionUtils.isEmpty(query.getSpecifications())) {
      int specIdx = 0;
      for (PresaleSaveQuery.SpecItem spec : query.getSpecifications()) {
        specIdx++;
        PresaleSpecification entity = new PresaleSpecification();
        entity.setPresaleId(presaleId);
        entity.setName(spec.getName());
        entity.setSkuCode(presale.getCode() + "-S" + specIdx);
        entity.setTicketType(spec.getTicketType());
        entity.setDeliveryType(spec.getDeliveryType());
        entity.setStock(spec.getStock());
        entity.setPoints(spec.getPoints());
        entity.setShipDays(spec.getShipDays());
        entity.setImages(spec.getImages());
        entity.setBonusTitle(spec.getBonusTitle());
        entity.setBonusImages(spec.getBonusImages());
        entity.setBonusDescription(spec.getBonusDescription());
        entity.setBonusQuantity(spec.getBonusQuantity());
        entity.setPriceItems(spec.getPriceItems());
        entity.setBonusIncluded(spec.getBonusIncluded() != null ? spec.getBonusIncluded() : true);
        presaleSpecificationMapper.insert(entity);
      }
      int totalPhysical = 0;
      for (PresaleSaveQuery.SpecItem spec : query.getSpecifications()) {
        if (spec.getDeliveryType() != null && spec.getDeliveryType() == 2) {
          totalPhysical += (spec.getStock() != null ? spec.getStock() : 0);
        }
      }
      presale.setTotalQuantity(totalPhysical);
      presaleMapper.updateById(presale);
    }
  }

  @Override
  @Transactional
  public void remove(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("presale id cannot be null");
    }
    presaleSpecificationMapper.delete(
      Wrappers.<PresaleSpecification>lambdaQuery().eq(PresaleSpecification::getPresaleId, id)
    );
    presaleMapper.deleteById(id);
  }

  private static String generateCode() {
    return "PRESALE-" + System.currentTimeMillis();
  }

  /** 从规格的 priceItems 推导主表 price（取首个规格的首档价格） */
  private BigDecimal derivePriceFromSpecs(List<PresaleSaveQuery.SpecItem> specs) {
    if (CollectionUtils.isEmpty(specs)) {
      return BigDecimal.ZERO;
    }
    for (PresaleSaveQuery.SpecItem spec : specs) {
      List<Map<String, Object>> items = spec.getPriceItems();
      if (items != null && !items.isEmpty()) {
        Object p = items.get(0).get("price");
        if (p instanceof Number) {
          return BigDecimal.valueOf(((Number) p).doubleValue());
        }
      }
    }
    return BigDecimal.ZERO;
  }

  private PresaleDetailResponse toDetailResponse(Presale p) {
    PresaleDetailResponse r = new PresaleDetailResponse();
    r.setId(p.getId());
    r.setCode(p.getCode());
    r.setTitle(p.getTitle());
    r.setDeliveryType(p.getDeliveryType());
    r.setDiscountMode(p.getDiscountMode());
    r.setPrice(p.getPrice());
    r.setAmount(p.getAmount());
    r.setTotalQuantity(p.getTotalQuantity());
    r.setLaunchTime(p.getLaunchTime());
    r.setEndTime(p.getEndTime());
    r.setUsageStart(p.getUsageStart());
    r.setUsageEnd(p.getUsageEnd());
    r.setPerUserLimit(p.getPerUserLimit());
    r.setMovieId(p.getMovieId());
    if (p.getMovieId() != null) {
      Movie movie = movieMapper.selectById(p.getMovieId());
      if (movie != null && movie.getName() != null) {
        r.setMovieName(movie.getName());
      }
    }
    r.setPickupNotes(p.getPickupNotes());
    r.setCover(p.getCover());
    r.setGallery(p.getGallery());
    return r;
  }

  private PresaleDetailResponse.SpecItem toSpecItem(PresaleSpecification s) {
    PresaleDetailResponse.SpecItem item = new PresaleDetailResponse.SpecItem();
    item.setId(s.getId());
    item.setName(s.getName());
    item.setSkuCode(s.getSkuCode());
    item.setTicketType(s.getTicketType());
    item.setDeliveryType(s.getDeliveryType());
    item.setStock(s.getStock());
    item.setPoints(s.getPoints());
    item.setShipDays(s.getShipDays());
    item.setImages(s.getImages() != null && !s.getImages().isEmpty() ? s.getImages() : Collections.emptyList());
    item.setBonusTitle(s.getBonusTitle());
    item.setBonusImages(s.getBonusImages() != null ? s.getBonusImages() : Collections.emptyList());
    item.setBonusDescription(s.getBonusDescription());
    item.setBonusQuantity(s.getBonusQuantity());
    item.setPriceItems(s.getPriceItems() != null ? s.getPriceItems() : Collections.emptyList());
    item.setBonusIncluded(s.getBonusIncluded() != null ? s.getBonusIncluded() : true);
    return item;
  }
}
