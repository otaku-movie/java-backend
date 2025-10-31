package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("cinema")
public class Cinema {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("name")
  String name;

  @TableField("description")
  String description;

  @TableField("address")
  String address;

  @TableField("tel")
  String tel;

  @TableField("home_page")
  String homePage;

  @TableField("max_select_seat_count")
  Integer maxSelectSeatCount;

  @TableField("brand_id")
  Integer brandId;

  @TableField("region_id")
  Integer regionId;

  @TableField("prefecture_id")
  Integer prefectureId;

  @TableField("latitude")
  Double latitude;

  @TableField("longitude")
  Double longitude;

  @TableField("postal_code")
  String postalCode;

  @TableField(value = "city_id", updateStrategy = FieldStrategy.IGNORED)
  Integer cityId;

  @TableField("full_address")
  String fullAddress;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
