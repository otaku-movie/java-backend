package com.example.backend.response.benefit;

import lombok.Data;

import java.util.Date;

/** MyBatis 查询行：特典-影院库存 + 影院信息 + 可选距离（米） */
@Data
public class BenefitCinemaAvailabilityRow {
  private Integer cinemaId;
  private String cinemaName;
  private String brandName;
  private String fullAddress;
  private Integer regionId;
  private Integer prefectureId;
  private Double latitude;
  private Double longitude;
  private Integer quota;
  private Integer remaining;
  private Integer manualSoldOut;
  /** 与用户定位的距离，单位米；无定位时为 null */
  private Double distanceMeters;
  /** 该影院、该影片（及重映维度）下未来场次数量，与 App 场次列表筛选一致 */
  private Integer showTimeCount;
  /** 最近一场开映时间 */
  private Date nearestShowTime;
  /** 最近若干场开映时间，逗号分隔的 "yyyy-MM-dd HH:mm:ss" 文本（按时间升序，最多 5 场） */
  private String upcomingShowTimes;
}
