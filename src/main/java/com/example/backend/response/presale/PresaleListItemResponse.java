package com.example.backend.response.presale;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PresaleListItemResponse {
  private Integer id;
  private String code;
  private String title;
  private Integer deliveryType;
  private Integer discountMode;
  private Integer mubitikeType;
  private BigDecimal price;
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
  /** 关联电影名称（列表展示用） */
  private String movieName;
  /** 主图（列表/详情展示） */
  private String cover;
  private List<String> gallery;
  private List<PresaleDetailResponse.SpecItem> specifications;
}
