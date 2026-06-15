package com.example.backend.response.app;

import lombok.Data;

/**
 * 批量查询「movieId → 出演演员」用的扁平行结构。
 *
 * 用在 {@code MovieMapper#getCastByMovieIds}，
 * Controller 拿到后按 {@link #movieId} 分组塞进 {@link NowMovieShowingResponse#getCast()}。
 */
@Data
public class MovieCastRow {
  private Integer movieId;
  private Integer id;
  private String name;
  private String cover;
}
