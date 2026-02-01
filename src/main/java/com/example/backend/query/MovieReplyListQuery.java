package com.example.backend.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class MovieReplyListQuery extends PaginationQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private Integer commentId;
}
