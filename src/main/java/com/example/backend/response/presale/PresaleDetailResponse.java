package com.example.backend.response.presale;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class PresaleDetailResponse {

  private Integer id;
  private String code;
  private String title;
  private Integer deliveryType;
  private Integer discountMode;
  private Integer mubitikeType;
  private BigDecimal price;
  private BigDecimal amount;
  private Integer totalQuantity;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date launchTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date endTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date usageStart;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date usageEnd;
  private Integer perUserLimit;
  private Integer movieId;
  /** 关联影片名称，用于展示 */
  private String movieName;
  private String pickupNotes;
  private String cover;
  private List<String> gallery;
  private List<SpecItem> specifications;

  @Data
  public static class SpecItem {
    private Integer id;
    private String name;
    private String skuCode;
    private Integer ticketType;
    private Integer deliveryType;
    private Integer stock;
    private Integer points;
    private Integer shipDays;
    /** 规格图集（多张） */
    private List<String> images;
    /** 规格级特典名称 */
    private String bonusTitle;
    /** 规格级特典图片URL数组 */
    private List<String> bonusImages;
    /** 规格级特典说明 */
    private String bonusDescription;
    /** 规格级特典数量 */
    private Integer bonusQuantity;
    /** 多档价格 [{label,price}] */
    private List<Map<String, Object>> priceItems;
    /** 是否含购票特典：true=特典あり，false=特典なし */
    private Boolean bonusIncluded;
  }
}
