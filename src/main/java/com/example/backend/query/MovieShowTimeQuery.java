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
  List<Integer> specIds;
  /** 放映类型 dict_item.id 或 code (2D/3D) */
  Integer dimensionType;
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  Integer movieVersionId;
}
