package com.example.backend.query;

import lombok.Data;

@Data
public class MovieStaffQuery {
  private Integer movieId;
  private Integer staffId;
  private Integer positionId;
}
