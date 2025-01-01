package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MovieCommentResponse {
  Integer id;
  String content;
  Integer commentUserId;
  String commentUserName;
  String commentUserAvatar;
  Integer movieId;

  Boolean like = false;
  Boolean unlike = false;
  Integer likeCount;
  Integer dislikeCount;
  Integer replyCount;
  List<MovieReplyResponse> reply;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date createTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date updateTime;
}
