package com.example.backend.query.benefit;

import lombok.Data;

import java.util.List;

@Data
public class BenefitCinemaAvailabilityQuery {
  private Integer benefitId;
  /** 特典关联影片，用于统计该影院未来场次；为 null 时场次数为 0 */
  private Integer movieId;
  /**
   * 与场次接口一致：非空则只统计该重映 id 的场次；为 null 则只统计 re_release_id IS NULL 的场次。
   */
  private Integer reReleaseId;
  /** 限定影院时非空；不限定时为 null */
  private List<Integer> whitelistIds;
  /** true 且 whitelistIds 非空时追加 IN 条件；true 且空列表由 Service 短路 */
  private boolean whitelistEnabled;
  private Integer regionId;
  private Integer prefectureId;
  private Integer cityId;
  private String keyword;
  /** remainingDesc | default | distance */
  private String sort;
  private Double latitude;
  private Double longitude;
  /** 当前登录用户 id。非空时，该用户收藏的影院在分页结果中全局置顶。 */
  private Integer favoriteUserId;
}
