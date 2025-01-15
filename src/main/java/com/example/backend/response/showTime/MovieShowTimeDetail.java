package com.example.backend.response.showTime;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;

@Data
public class MovieShowTimeDetail {
  Integer id;
  String startTime;
  String endTime;
  Integer status;
  Integer movieId;
  String movieName;
  String moviePoster;
  Integer cinemaId;
  String cinemaName;
  Integer theaterHallId;
  String theaterHallName;
  long selectedSeatCount;
  List<Integer> subtitleId;
  List<Integer> movieShowTimeTagsId;
  List<Language> subtitle;
  List<MovieShowTimeTag> movieShowTimeTags;
  Integer specId;
  String specName;
}
