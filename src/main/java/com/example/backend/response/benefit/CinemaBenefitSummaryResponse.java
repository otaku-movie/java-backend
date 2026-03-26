package com.example.backend.response.benefit;

import lombok.Data;

import java.util.List;

/** 按电影查询时，某影院的特典汇总：各物料配额/剩余、用户反馈数 */
@Data
public class CinemaBenefitSummaryResponse {
  private Integer cinemaId;
  private String cinemaName;
  /** 该影院在该电影下的特典物料明细（阶段+物料+配额+剩余） */
  private List<CinemaBenefitItemSummary> items;
  /** 总配额（所有物料配额之和） */
  private Integer totalQuota;
  /** 总剩余（所有物料剩余之和，null 表示有未维护的） */
  private Integer totalRemaining;
  /** 该影院下用户「已领完」反馈条数（用于提醒运营核实） */
  private Integer feedbackCount;
}
