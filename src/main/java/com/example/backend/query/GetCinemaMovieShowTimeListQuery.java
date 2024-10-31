package com.example.backend.query;

import lombok.Data;

@Data
public class GetCinemaMovieShowTimeListQuery {
  Integer cinemaId;
  Integer movieId;
}
