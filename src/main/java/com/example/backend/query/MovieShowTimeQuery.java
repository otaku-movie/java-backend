package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieShowTimeQuery {
  Integer id;
  @NotNull
  Integer cinemaId;
  @NotNull
  Integer theaterHallId;
  @NotNull
  Integer movieId;
  Boolean open;
  @NotNull
  String startTime;
  @NotNull
  String endTime;
}
