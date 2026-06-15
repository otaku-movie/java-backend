package com.example.backend.response.movie;

import lombok.Data;

/**
 * 重复电影候选组里的单行（也用于手动搜索结果）。
 */
@Data
public class MovieDuplicateItem {
  private Integer id;
  private String name;
  private String movieKey;
  private Integer tmdbId;
  private Integer deleted;
  private String kind;
  private String releaseDate;
  /** 该行当前挂着的未删除场次数（合并时用来判断主次）。 */
  private Long showCount;
  /** 各关联表计数摘要，用于合并前 diff（例：show=12,comment=1,rate=3）。 */
  private String referenceSummary;
}
