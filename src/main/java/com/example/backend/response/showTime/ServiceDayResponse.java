package com.example.backend.response.showTime;

import lombok.Data;

/** 某日适用的服务日（仅按日历规则匹配，与场次时段无关）。 */
@Data
public class ServiceDayResponse {
  /** service_day_code，供前端 i18n（如 wednesday、first_day）。 */
  private String code;
  /** 票种原名（如 TOHOウェンズデイ），优先用于 Tab 下展示。 */
  private String name;
  /** 是否会员日（Ponta 木曜等）。 */
  private Boolean member;
}
