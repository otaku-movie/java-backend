package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Date;

@Data
public class MovieReplyResponse {
  Integer id;
  String content;
  Integer commentId;
  Integer commentUserId;
  String commentUserName;
  String commentUserAvatar;
  Integer replyUserId;
  String replyUserName;
  String replyUserAvatar;
  Integer movieCommentId;
  Boolean like;
  Boolean dislike;
  Integer likeCount;
  Integer dislikeCount;
  String parentReplyId;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date createTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date updateTime;
}
