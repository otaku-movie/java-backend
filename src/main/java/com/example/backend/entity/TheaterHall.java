package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("theater_hall")
public class TheaterHall {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("name")
  String name;

  @TableField("seat_naming_rules")
  String seatNamingRules;

  @TableField("row_count")
  Integer rowCount;

  @TableField("column_count")
  Integer columnCount;

  @TableField("cinema_id")
  Integer cinemaId;

  @TableField("cinema_spec_id")
  Integer cinemaSpecId;

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
