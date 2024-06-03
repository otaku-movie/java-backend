package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieCommentListQuery {
  private Integer page;
  private Integer pageSize;
  @NotNull
  private Integer movieId;

  public MovieCommentListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}
