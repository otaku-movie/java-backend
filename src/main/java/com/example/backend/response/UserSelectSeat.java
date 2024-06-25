package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserSelectSeat {
  @JsonProperty("user_id")
  private Integer userId;

  @JsonProperty("movie_show_time_id")
  private Integer movieShowTimeId;

  @JsonProperty("theater_hall_id")
  private Integer theaterHallId;

  private Integer x;
  private Integer y;

  @JsonProperty("area_price")
  private BigDecimal areaPrice;

  @JsonProperty("area_name")
  private String areaName;

  @JsonProperty("movie_id")
  private Integer movieId;

  @JsonProperty("movie_name")
  private String movieName;

  @JsonProperty("movie_poster")
  private String moviePoster;

  @JsonProperty("spec_name")
  private String specName;

  @JsonProperty("plus_price")
  private BigDecimal plusPrice;
}
