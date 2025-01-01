package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("movie_comment_reaction")
public class MovieCommentReaction {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField(value = "\"like\"") // 使用双引号括起来
  Boolean like;

  @TableField(value = "\"dislike\"") // 使用双引号括起来
  Boolean dislike;

  // 评论id  类型： comment reply
  @TableField("type")
  String type;

  // 评论id
  @TableField("user_id")
  Integer userId;

  // 评论id
  @TableField("movie_comment_id")
  Integer movieCommentId;

  // 回复id
  @TableField("movie_reply_id")
  Integer movieReplyId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  Date updateTime;

}
