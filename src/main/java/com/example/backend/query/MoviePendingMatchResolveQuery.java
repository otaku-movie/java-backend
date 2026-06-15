package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 后台处理待确认重复匹配：合并或忽略。 */
@Data
public class MoviePendingMatchResolveQuery {
  @NotNull
  private Long pendingId;

  /** merge / ignore */
  @NotNull
  private String action;

  /** action=merge 时必填。 */
  private Integer survivorId;

  /** action=merge 时必填；通常就是待确认对里的另一个 movie id。 */
  private List<Integer> loserIds;

  /** action=merge 时可选。 */
  private String newName;

  /** action=merge 时可选：详情对比页「逐项应用」的字段级覆盖。 */
  private MovieMergeFieldOverrides fieldOverrides;
}
