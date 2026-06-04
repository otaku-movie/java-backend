package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * C 端用户收藏影院。绑定 cinema_id（importer 按 cinema_key upsert，保持 id 稳定）。
 */
@Data
@TableName("user_favorite_cinema")
public class UserFavoriteCinema {
  @TableId(value = "id", type = IdType.AUTO)
  Long id;

  @TableField("user_id")
  Integer userId;

  @TableField("cinema_id")
  Integer cinemaId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @TableField("deleted")
  @TableLogic
  Integer deleted;
}
