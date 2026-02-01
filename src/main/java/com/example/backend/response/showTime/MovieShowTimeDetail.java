package com.example.backend.response.showTime;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import lombok.Data;

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
  /** 规格ID列表（多选），兼容用第一个参与加价计算 */
  List<Integer> specIds;
  /** 第一个规格ID，用于加价等计算 */
  Integer specId;
  /** 规格名称（多个时取第一个或拼接） */
  String specName;
  /** 放映类型：dict_item.id (2D/3D) */
  Integer dimensionType;
  Integer movieVersionId;
  Integer versionCode;  // 配音版本ID（字典值）
}
