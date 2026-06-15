package com.example.backend.query;

import lombok.Data;

/**
 * 「电影去重合并」字段级覆盖。
 *
 * <p>用于详情对比页的「逐项应用」：前端在每个字段上选定要保留的值（可来自任意候选行），
 * 点「合并」时把最终值一并提交，后端在合并事务内写回 survivor。</p>
 *
 * <p>语义：字段为 {@code null} 表示「不改动，保留 survivor 原值」；非 null 则覆盖写入。
 * 因此前端只需提交用户实际「应用」过的字段。</p>
 *
 * <p>仅开放标量字段。{@code movie_key}（唯一键，冲突风险）、{@code kind}（类型位）
 * 等身份字段不在覆盖范围内。聚合引用数据（场次 / 演职员 / 角色 / 各类计数）通过外键
 * 重指向自动合并，不在此处理。</p>
 */
@Data
public class MovieMergeFieldOverrides {

  /** 名称；非空白才会覆盖（movie.name 为 NOT NULL，避免被清空）。 */
  private String name;

  /** 原名 original_name。 */
  private String originalName;

  /** 上映日 release_date，格式 yyyy-MM-dd；非空白才会覆盖。 */
  private String releaseDate;

  /** 片长（分钟），对应 movie.time。 */
  private Integer runtime;

  /** 海报地址；写回展示优先列 movie.cover_url。 */
  private String cover;

  /** 简介 description。 */
  private String description;

  /** TMDB id（movie.tmdb_id 为普通索引，非唯一，可安全覆盖）。 */
  private Integer tmdbId;

  /** 分级 movie.level_id。 */
  private Integer levelId;
}
