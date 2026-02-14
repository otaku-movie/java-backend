package com.example.backend.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserSelectSeat {
  private Integer movieShowTimeId;
  private Integer movieId;


  private String movieName;


  private String moviePoster;


  private String date;


  private String startTime;


  private String endTime;


  private String specName;


  private Integer cinemaId;


  private String cinemaName;


  private Integer theaterHallId;

  private String theaterHallName;

  /** 放映类型 dict_item.id，1=2D 2=3D */
  private Integer dimensionType;

  /** 规格ID列表（IMAX/Dolby等） */
  private List<Integer> specIds;

  /** 是否支持ムビチケ等前売り券 */
  private Boolean allowPresale;

  /** 定价模式：1=系统活动 2=固定价格 */
  private Integer pricingMode;

  /** 固定价格模式下的单价（pricing_mode=2 时使用） */
  private BigDecimal fixedAmount;

  /** 规格名称+加价列表（用于展示） */
  private List<SpecPriceItem> specPriceList;

  /** 放映类型名称（2D/3D） */
  private String displayTypeName;

  /** 放映类型加价（3D 加价，2D 为 0） */
  private BigDecimal displayTypeSurcharge;

  /** 影院规格加价（首个规格，兼容旧逻辑） */
  private BigDecimal plusPrice;

  List<UserSelectSeatList> seat;
}
