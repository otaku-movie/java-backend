package com.example.backend.query;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 「电影去重合并」详情对比页请求：批量拉取若干候选电影的完整信息。
 */
@Data
public class MovieMergeDetailQuery {
  /** 要对比的候选电影 id 列表。 */
  @NotEmpty
  private List<Integer> ids;
}
