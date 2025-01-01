package com.example.backend.query;

import lombok.Data;

@Data
public class MovieReplyListQuery extends PaginationQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer commentId;
}
