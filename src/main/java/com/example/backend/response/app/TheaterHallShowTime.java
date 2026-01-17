package com.example.backend.response.app;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import lombok.Data;

import java.util.List;

@Data
public class TheaterHallShowTime {
  Integer id;
  Integer theaterHallId;
  String theaterHallName;
  String startTime;
  String endTime;
  String specName;
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  List<Language> subtitle;
  List<MovieShowTimeTag> showTimeTags;
  Integer movieVersionId;
  Integer versionCode;
}
