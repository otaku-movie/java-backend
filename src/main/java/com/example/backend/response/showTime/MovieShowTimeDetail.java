package com.example.backend.response.showTime;

import com.example.backend.entity.Language;
import com.example.backend.entity.MovieShowTimeTag;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
  /** 放映类型：dict_item.id 或 code (2D/3D) */
  Integer dimensionType;
  Integer movieVersionId;
  Integer versionCode;  // 配音版本ID（字典值）
  /** 是否开放选座（管理端编辑回填） */
  Boolean open;
  /** 定时公开时间 yyyy-MM-dd HH:mm:ss */
  String publishAt;
  /** 开放购票时间 yyyy-MM-dd HH:mm:ss */
  String saleOpenAt;
  /** 定价模式：1=系统活动 2=固定价格 0=默认规则 */
  Integer pricingMode;
  /** 固定价格模式下的基础票价 */
  BigDecimal fixedAmount;
  /** 规格补价(3D/IMAX等) */
  BigDecimal surcharge;
  /** 是否支持前售券 */
  Boolean allowPresale;
  /** 默认规则下本场次票种覆盖价：票种id -> 价格（编辑回填） */
  Map<Integer, BigDecimal> ticketTypeOverrides;
  /** 默认规则下本场次票种是否启用：票种id -> 是否启用（编辑回填） */
  Map<Integer, Boolean> ticketTypeEnabled;
}
