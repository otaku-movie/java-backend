package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.backend.response.MovieReplyResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("movie_reply")
public class MovieReply extends MovieReplyResponse {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("content")
  String content;

  @TableField("like_count")
  Integer likeCount;

  @TableField("dislike_count")
  Integer dislikeCount;

  @TableField("movie_id")
  Integer movieId;

  // 父级回复id
  @TableField("parent_reply_id")
  String parentReplyId;

  // 回复人
  @TableField("comment_user_id")
  Integer commentUserId;

  // 评论人
  @TableField("reply_user_id")
  Integer replyUserId;

  @TableField("movie_comment_id")
  Integer movieCommentId;

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
