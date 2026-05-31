package com.example.backend.response.app;

import lombok.Data;

/**
 * 分享落地页 - 订单/票根响应（公开可见字段）。
 *
 * <p>注意：此接口对任何人公开，<b>绝不能返回</b>用户身份、座位号、支付金额等隐私字段。
 * 只能携带「这部电影 + 哪一场 + 哪家影院」这类用于让朋友看到「我要去看」的元信息。</p>
 */
@Data
public class OrderShareDetailResponse {
  private String orderNumber;

  private Integer movieId;
  private String movieName;
  private String moviePoster;

  /** 场次日期，例如 2026-06-12 */
  private String date;
  /** 开场时间，例如 18:30 */
  private String startTime;

  private String cinemaName;
  private String cinemaCity;
}
