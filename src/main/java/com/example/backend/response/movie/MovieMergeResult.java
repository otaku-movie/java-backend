package com.example.backend.response.movie;

import lombok.Data;

/**
 * 合并结果回包。
 */
@Data
public class MovieMergeResult {
  private Integer survivorId;
  private String survivorName;
  /** 实际被合并并软删的 loser 行数。 */
  private int mergedCount;
  /** survivor 合并后挂着的未删除场次数（供前端核对）。 */
  private Long survivorShowCount;
}
