package com.example.backend.response;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class CinemaResponse {
  Integer id;
  String name;
  String description;
  String address;
  String tel;
  String homePage;
  Integer maxSelectSeatCount;
  Integer theaterCount;
  Integer brandId;
  String brandName;
  List<Spec> spec;
  private Integer regionId;
  private Integer prefectureId;
  private Integer cityId;
  private String fullAddress;
  private Double latitude;
  private Double longitude;
  private String postalCode;
  // 距离（米），仅在附近查询时返回
  private Double distance;
  // 新增：当前上映的电影列表
  List<com.example.backend.response.cinema.MovieShowingResponse> nowShowingMovies;
}
