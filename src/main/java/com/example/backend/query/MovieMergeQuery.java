package com.example.backend.query;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 合并重复电影请求。
 *
 * <p>语义：把 {@code loserIds} 里的若干「重复行」合并进 {@code survivorId}：
 * 所有引用这些 loser 的业务外键（场次 / 重映 / 规格 / 演职员 / 评论 / 评分 …）
 * 重指向 survivor；loser 软删；survivor 复活（deleted=0）并可选改名。</p>
 */
@Data
public class MovieMergeQuery {
  /** 存活行 id（合并目标）。 */
  @NotNull
  private Integer survivorId;

  /** 被合并并软删的重复行 id 列表。 */
  @NotEmpty
  private List<Integer> loserIds;

  /** 可选：合并后存活行的新名字；空 / 空白则保留原名。 */
  private String newName;

  /**
   * 可选：字段级覆盖（详情对比页「逐项应用」的结果）。
   * 为空则只做名字（newName）处理，其余字段保持 survivor 原值。
   */
  private MovieMergeFieldOverrides fieldOverrides;
}
