package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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
  @NotNull
  Integer specId;
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  Integer movieVersionId;
}
