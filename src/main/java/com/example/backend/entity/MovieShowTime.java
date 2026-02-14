package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.typeHandler.IntegerArrayTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("movie_show_time")
public class MovieShowTime {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("movie_id")
  Integer movieId;

  @TableField("cinema_id")
  Integer cinemaId;

  @TableField("theater_hall_id")
  Integer theaterHallId;

  @TableField("open")
  Boolean open;

  @TableField("start_time")
  String startTime ;

  @TableField("end_time")
  String endTime;

  @TableField("status")
  Integer status;

  @TableField(value = "subtitle_id", typeHandler = IntegerArrayTypeHandler.class)
  List<Integer> subtitleId;

  @TableField(value = "show_time_tag_id", typeHandler = IntegerArrayTypeHandler.class)
  List<Integer>  showTimeTagId;

  @TableField(value = "spec_ids", typeHandler = IntegerArrayTypeHandler.class)
  List<Integer> specIds;

  /** 放映类型：dict_item.code (dict.code=dimensionType)，1=2D 2=3D */
  @TableField("dimension_type")
  Integer dimensionType;

  @TableField("movie_version_id")
  Integer movieVersionId;

  /** 定价模式：1=系统活动模式(按规则匹配) 2=固定价格模式 */
  @TableField("pricing_mode")
  Integer pricingMode;

  /** 固定价格模式下的基础票价，pricing_mode=2 时使用 */
  @TableField("fixed_amount")
  java.math.BigDecimal fixedAmount;

  /** 规格补价(3D/IMAX 等)，最终价=基础价+surcharge */
  @TableField("surcharge")
  java.math.BigDecimal surcharge;

  /** 是否支持前售券(ムビチケ等) */
  @TableField("allow_presale")
  Boolean allowPresale;

  /** 定时公开时间（null 表示立即公开） */
  @TableField("publish_at")
  String publishAt;

  /** 开放购票时间 */
  @TableField("sale_open_at")
  String saleOpenAt;

  /** 是否可购票（基于 sale_open_at 由定时任务维护） */
  @TableField("can_sale")
  Boolean canSale;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}