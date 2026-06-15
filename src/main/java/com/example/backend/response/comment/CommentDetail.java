package com.example.backend.response.comment;

import com.example.backend.entity.MovieComment;
import com.example.backend.entity.MovieReply;
import com.example.backend.response.MovieReplyResponse;
import lombok.Data;

import java.util.List;

@Data
public class CommentDetail {
  Integer id;
  String content;
  Integer commentUserId;
  String commentUserName;
  String commentUserAvatar;
  boolean like;
  boolean dislike;
  String createTime;
  String updateTime;
  Integer dislikeCount;
  Integer likeCount;
  Integer replyCount;
  Integer movieId;
  // 是否含剧透：0=否 1=是
  Integer spoiler;
  double rate;
  List<MovieReplyResponse> reply;
}
