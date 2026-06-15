package com.example.backend.response.movie;

import lombok.Data;

import java.math.BigDecimal;

/** pending_movie_match 原始行（service 会再组装成带两条电影 item 的回包）。 */
@Data
public class MoviePendingMatchRow {
  private Long id;
  private Integer candidateMovieId;
  private Integer matchedMovieId;
  private String candidateTitle;
  private String matchedTitle;
  private BigDecimal confidence;
  private String matchReason;
  private String status;
  private String createTime;
  private String updateTime;
}
