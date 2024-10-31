package com.example.backend.query.app;

import com.example.backend.query.PaginationQuery;
import lombok.Data;

@Data
public class AppMovieListQuery extends PaginationQuery {
  private String name;
}