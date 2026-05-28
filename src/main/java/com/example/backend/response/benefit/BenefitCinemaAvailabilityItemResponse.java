package com.example.backend.response.benefit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BenefitCinemaAvailabilityItemResponse {
  private Integer cinemaId;
  private String cinemaName;
  private String brandName;
  private String fullAddress;
  private Integer regionId;
  private Integer prefectureId;
  private Double latitude;
  private Double longitude;
  /** 与用户距离，单位 km；无定位时为 null */
  private Double distanceKm;
  private Integer quota;
  private Integer remaining;
  /** 字典 benefitStockStatus：1充足 2少量 3极少 4已领完 5未知 6用户反馈领完 */
  private Integer stockStatus;
  private Integer feedbackCount;
  private Integer feedbackWindowHours;
  /**
   * 当前登录用户是否已对该影院+特典提交过反馈（未登录恒为 false）。
   * 与 Redis 用户集 + DB 归档一致，供 App 列表「已反馈」态；匿名仅看聚合计数。
   */
  private boolean currentUserFeedbackSubmitted;
  /** 未来场次数（与 POST /api/app/cinema/movie/showTime 同一 movie / reRelease 维度） */
  private Integer showTimeCount;
  /** 最近一场开映时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
  private Date nearestShowTime;
}
