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
  /**
   * 放映类型：dict_item.id，2D/3D。
   * ShowTimeDetail 需要与列表页一致展示该 chip。
   */
  Integer dimensionType;
  List<Integer> subtitleId;
  List<Integer> showTimeTagId;
  List<Language> subtitle;
  List<MovieShowTimeTag> showTimeTags;
  Integer movieVersionId;
  Integer versionCode;
  /** 特殊场次名/活动名（来自标题装饰，普通场次为空）。 */
  String eventTitle;
  /**
   * 影院官方购票页 URL（来自爬虫 reservation_params_json.reservation_url）。
   * app 端点击「购票」时优先打开此链接，外跳到影院官网；为 null 时回退到自家选座流程。
   */
  String reservationUrl;
  /**
   * 爬虫透传的售票/座位状态：on_sale / few / sold_out / pre_sale / sale_ended / closed / unknown。
   * app 端用此字段判断"官方还未开放购票"，在 ShowTimeDetail 卡片中禁用「购票」按钮，
   * 避免用户跳到官网遇到 ERR-2002 等错误页。
   */
  String saleStatus;
}
