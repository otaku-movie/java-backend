package com.example.backend.response.app;

import lombok.Data;

/**
 * 批量查询「movieId → 上映规格(IMAX/4DX 等)」用的扁平行结构。
 *
 * 用在 {@code MovieMapper#getSpecsByMovieIds}，
 * Controller 拿到后按 {@link #movieId} 分组塞进 {@link NowMovieShowingResponse#getSpec()}。
 */
@Data
public class MovieSpecRow {
  private Integer movieId;
  private Integer id;
  private String name;
  private String description;
}
