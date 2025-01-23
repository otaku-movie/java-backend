package com.example.backend.service;

import com.example.backend.entity.MovieShowTime;
import com.example.backend.query.MovieShowTimeQuery;

import java.text.ParseException;
import java.util.List;

public interface MovieShowTimeService  {
  List<MovieShowTime> getSortedMovieShowTimes(MovieShowTimeQuery query, String format);
  void saveMovieShowTimeIfNotExists(MovieShowTimeQuery query, String format) throws ParseException;
  boolean check(List<MovieShowTime> list, String format, MovieShowTimeQuery query);


}
