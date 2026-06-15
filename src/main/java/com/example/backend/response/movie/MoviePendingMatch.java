package com.example.backend.response.movie;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 后台「待确认」重复匹配回包。 */
@Data
public class MoviePendingMatch {
  private Long id;
  private BigDecimal confidence;
  private String matchReason;
  private String status;
  private String createTime;
  private String updateTime;
  private Integer recommendedSurvivorId;
  private List<MovieDuplicateItem> items;
}
