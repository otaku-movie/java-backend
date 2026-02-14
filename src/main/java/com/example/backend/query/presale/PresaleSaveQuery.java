package com.example.backend.query.presale;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class PresaleSaveQuery {

  private Integer id;

  /** 编码，新建可不传（自动生成 PRESALE-xxx） */
  private String code;

  @NotEmpty(message = "{validator.presale.title.required}")
  private String title;

  /** dict_item.code，presaleDeliveryType：1=虚拟 2=实体 */
  @NotNull(message = "{validator.presale.deliveryType.required}")
  private Integer deliveryType;

  /** dict_item.code，presaleDiscountMode：1=固定 2=比例 */
  private Integer discountMode;

  /** dict_item.code，presaleMubitikeType：1=网络券 2=实体券 3=套票 4=电影票预售券 */
  @NotNull(message = "{validator.presale.mubitikeType.required}")
  private Integer mubitikeType;

  /** 实际结算金额/折后价，未传时由规格 priceItems 推导 */
  private BigDecimal amount;

  /** 实体券总库存（仅统计 deliveryType=2 的规格 stock 之和），未传时由规格推导 */
  @NotNull(message = "{validator.presale.totalQuantity.required}")
  private Integer totalQuantity;

  /** 开始发售时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date launchTime;
  /** 结束发售时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date endTime;
  /** 使用开始时间（通常为上映当天） */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date usageStart;
  /** 使用结束时间（通常为上映结束或上映+60天） */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date usageEnd;

  /** 每人限购数量，0 表示不限制 */
  private Integer perUserLimit;

  /** 适用电影ID（仅一个） */
  private Integer movieId;

  private String pickupNotes;
  private String cover;
  private List<String> gallery;

  @Valid
  private List<SpecItem> specifications;

  @Data
  public static class SpecItem {
    private Integer id;
    private String name;
    private String skuCode;
    private Integer ticketType;
    @NotNull(message = "{validator.presale.spec.deliveryType.required}")
    private Integer deliveryType;
    /** 库存数量，实体券时计入 totalQuantity */
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
    /** 是否含购票特典：true=特典あり，false=特典なし，默认 true */
    private Boolean bonusIncluded;
  }
}
