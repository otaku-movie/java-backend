package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisType {
  // 验证码
  verifyCode("verifyCode"),
  // 用户评论的点赞
  userMovieCommentLike("movie:comment:like"),
  userMovieCommentLikeCount("movie:comment:like:count"),
  // 用户评论的点踩
  userMovieCommentDislike("movie:comment:dislike"),
  userMovieCommentDislikeCount("movie:comment:dislike:count"),

  // 用户回复的点赞
  userMovieReplyLike("movie:reply:like"),
  userMovieReplyLikeCount("movie:reply:like:count"),
  // 用户回复的点踩
  userMovieReplyDislike("movie:reply:dislike"),
  userMovieReplyDislikeCount("movie:reply:dislike:count");

  private final String code;
}
