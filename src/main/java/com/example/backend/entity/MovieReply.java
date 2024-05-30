package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("movie_reply")
public class MovieReply {
  @TableId(value = "id", type = IdType.AUTO)
  Integer id;

  @TableField("content")
  String content;

  // 父级回复id
  @TableField("parent_reply_id")
  Integer parentReplyId;

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
