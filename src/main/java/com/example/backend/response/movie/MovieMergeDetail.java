package com.example.backend.response.movie;

import lombok.Data;

import java.util.List;

/**
 * 「电影去重合并」详情对比页用：单部候选电影的完整信息。
 *
 * <p>除基础信息外，重点提供能帮助人工判断「是否同一部 / 哪部更全」的明细：
 * 各关联表的结构化计数、场次按影院的分布、staff 与角色名单样本。</p>
 */
@Data
public class MovieMergeDetail {
  private Integer id;
  private String name;
  private String originalName;
  private String movieKey;
  private Integer tmdbId;
  private Integer deleted;
  private String kind;
  private String releaseDate;
  /** 片长（分钟）。 */
  private Integer runtime;
  /** 海报：优先 R2 镜像地址 coverUrl，回退原始 cover。 */
  private String cover;
  private String description;

  /** 分级 movie.level_id 及其名称（来自 level 表）。 */
  private Integer levelId;
  private String levelName;

  /** 各关联表的结构化计数（用于并排 diff）。 */
  private Counts counts;

  /** 场次按影院的分布（取场次最多的前若干家）。 */
  private List<ShowtimeByCinema> showtimesByCinema;

  /** staff 名单样本（含职位）。 */
  private List<StaffBrief> staff;

  /** 角色名单样本。 */
  private List<String> characters;

  /** 版本列表（原版 / 配音 + 语言）。 */
  private List<VersionBrief> versions;

  /** 标签名列表。 */
  private List<String> tags;

  /** 规格名列表（IMAX / 4DX 等，来自 cinema_spec）。 */
  private List<String> specs;

  @Data
  public static class Counts {
    private Long show;
    private Long reRelease;
    private Long benefit;
    private Long presale;
    private Long comment;
    private Long rate;
    private Long staff;
    private Long character;
    private Long spec;
    private Long tag;
    private Long version;
  }

  @Data
  public static class ShowtimeByCinema {
    private Integer cinemaId;
    private String cinemaName;
    private Long count;
    private String firstDate;
    private String lastDate;
  }

  @Data
  public static class StaffBrief {
    private String name;
    private String position;
  }

  @Data
  public static class VersionBrief {
    /** 1=原版 2=配音版（dub_version / version_code）。 */
    private Integer versionCode;
    /** 语言名（language 表）。 */
    private String language;
  }
}
