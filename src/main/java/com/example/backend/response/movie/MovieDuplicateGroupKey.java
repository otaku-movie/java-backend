package com.example.backend.response.movie;

import lombok.Data;

/**
 * 重复候选「分组键」——先分页取出组键，再按键拉取组内成员。
 */
@Data
public class MovieDuplicateGroupKey {
  /** {@code "same_tmdb"} / {@code "same_name"}。 */
  private String groupType;
  /** tmdb_id 文本 或 lower(btrim(name))。 */
  private String groupValue;
}
