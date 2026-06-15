package com.example.backend.response.movie;

import lombok.Data;

import java.util.List;

/**
 * 一组「疑似同一部电影」的重复候选行。
 */
@Data
public class MovieDuplicateGroup {
  /** 分组依据：{@code "same_tmdb"}（同 tmdb_id）/ {@code "same_name"}（名字完全相同）。 */
  private String reason;
  /** 分组值（tmdb_id 文本或归一名字），仅用于前端展示/调试。 */
  private String groupValue;
  /** 推荐存活行 id：优先带 tmdb_id 的规范行，其次活跃行，再次最小 id。 */
  private Integer recommendedSurvivorId;
  /** 组内所有行（含已软删），按 tmdb 优先 / 活跃优先 / id 升序。 */
  private List<MovieDuplicateItem> items;
}
