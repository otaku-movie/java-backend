package com.example.backend.query;

import lombok.Data;

/**
 * 手动搜索可合并电影（后台「电影去重合并」页 - 手动模式）。
 * 支持按名字 ILIKE、movie_key 精确、或 id / tmdb_id 数字匹配。
 */
@Data
public class MovieMergeSearchQuery {
  /** 关键词：名字片段 / movie_key / 数字(id 或 tmdb_id)。 */
  private String keyword;
}
