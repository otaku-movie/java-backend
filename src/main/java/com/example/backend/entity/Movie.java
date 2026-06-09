package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("movie")
public class Movie {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("movie_key")
  String movieKey;

  @TableField("cover")
  String cover;

  /**
   * Cloudflare R2 上的 WebP 海报地址；NULL 表示还没镜像，前端兜底用 cover。
   * 由 `npm run mirror:images` 脚本写入。
   */
  @TableField("cover_url")
  String coverUrl;

  @TableField("name")
  String name;

  @TableField("original_name")
  String originalName;

  @TableField("description")
  String description;

  @TableField("level_id")
  Integer levelId;

  @TableField("home_page")
  String homePage;

  @TableField("start_date")
  String startDate;

  @TableField("end_date")
  String endDate;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("status")
  Integer status;

  // 1 未上映 2 上映中 3 上映结束
  @TableField("time")
  Integer time;

  @TableField("watched_count")
  Integer watchedCount;

  @TableField("want_to_see_count")
  Integer wantToSeeCount;

  /**
   * 内容类型，区分电影与非电影（ODS：演唱会 / 体育 / 舞台 / Live Film 等）。
   *  - {@code "movie"}（默认）：常规剧场公映片
   *  - {@code "ods"}：非电影类放映（演唱会、宝塚 / 歌舞伎中継、Live Film…）
   */
  @TableField("kind")
  String kind;

  @TableField("credits_locked")
  Boolean creditsLocked;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

  @JsonIgnore
  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
