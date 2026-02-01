package com.example.backend.enumerate;

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
  userMovieReplyDislikeCount("movie:reply:dislike:count"),

  // 选座锁定（临时锁定，等待支付）
  seatSelectionLock("seat:selection:lock"),
  // 选座数据（存储选座信息，用于快速查询）
  seatSelectionData("seat:selection:data"),
  // 订单创建锁（防止重复创建订单）
  orderCreateLock("order:create:lock"),
  // 支付锁（防止同一订单重复支付）
  orderPaymentLock("order:payment:lock"),
  // 支付限流（按用户）
  paymentRateLimit("payment:rate:limit"),
  // 选座保存锁（防止重复保存选座）
  seatSelectionSaveLock("seat:selection:save:lock"),
  // 座位列表缓存（缓存某场次某影厅的座位列表）
  seatListCache("seat:list:cache");

  private final String code;

  RedisType(String code) { this.code = code; }

  public String getCode() { return code; }
}
