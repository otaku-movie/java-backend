package com.example.backend.query;

import java.util.Set;
import lombok.Data;

@Data
public class MovieListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  /** 用于重映列表等按 movieId 过滤的场景（兼容老的 id 字段） */
  private Integer movieId;
  private Integer status;
  private String name;
  /**
   * 是否筛选“存在重映计划”的电影：
   * - null：不筛选
   * - 1：仅重映过
   * - 0：仅未重映
   */
  private Integer hasReRelease;

  /**
   * 内容类型过滤（对应 movie.kind）：
   * - null / "movie"：仅返回电影（默认，过滤掉演唱会 / 舞台 / 体育转播等 ODS）
   * - "ods"：仅返回 ODS 内容
   * - "all"：不过滤
   * 其余取值在 {@link #getKind()} 处被规范化，无效输入按 "movie" 处理。
   */
  private String kind;

  private static final Set<String> ALLOWED_KIND = Set.of("movie", "ods", "all");

  /**
   * 表格排序字段（与前端列 dataIndex 一一对应）。
   * 允许值见 {@link #ALLOWED_SORT_FIELDS}，其它输入会在 getter 处被忽略
   * （白名单防止 SQL 注入；mapper SQL 直接拼接 {@code ${sortField}}）。
   */
  private String sortField;

  /** 排序方向：{@code asc} / {@code desc}，其它值忽略。 */
  private String sortOrder;

  /**
   * 与 mapper xml 内的 {@code <choose>} 分支一一对应。
   * 新增可排序列时同时更新这里 + xml + 前端列定义。
   */
  public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "cinemaCount",
      "theaterCount",
      "commentCount",
      "watchedCount",
      "wantToSeeCount",
      "startDate",
      "endDate"
  );

  public MovieListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }

  /** 白名单过滤：不在允许列表内的 sortField 一律返回 null，让 mapper 走默认排序。 */
  public String getSortField() {
    if (sortField == null || !ALLOWED_SORT_FIELDS.contains(sortField)) {
      return null;
    }
    return sortField;
  }

  /** 方向只允许 asc / desc，其它返回 null，让 mapper 走默认排序。 */
  public String getSortOrder() {
    if (sortOrder == null) return null;
    String o = sortOrder.trim().toLowerCase();
    if ("asc".equals(o) || "desc".equals(o)) return o;
    return null;
  }

  /**
   * kind 规范化：未传 / 不在白名单 → 默认 "movie"（隐藏 ODS）。
   * mapper 拿到 "all" 时跳过过滤；拿到 "movie"/"ods" 时拼到 SQL。
   */
  public String getKind() {
    if (kind == null) return "movie";
    String k = kind.trim().toLowerCase();
    if (ALLOWED_KIND.contains(k)) return k;
    return "movie";
  }
}