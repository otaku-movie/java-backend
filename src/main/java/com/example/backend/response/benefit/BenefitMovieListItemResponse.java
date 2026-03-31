package com.example.backend.response.benefit;

import lombok.Data;

@Data
public class BenefitMovieListItemResponse {
  private Integer movieId;
  private String movieName;
  private String movieCover;
  private Integer benefitCount;
}

