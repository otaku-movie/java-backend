package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
  private Integer areaPrice;

  @JsonProperty("area_name")
  private String areaName;

  @JsonProperty("movie_id")
  private Integer movieId;

  @JsonProperty("movie_name")
  private String movieName;

  @JsonProperty("movie_poster")
  private String moviePoster;
}
