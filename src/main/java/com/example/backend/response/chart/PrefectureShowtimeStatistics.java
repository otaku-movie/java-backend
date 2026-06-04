package com.example.backend.response.chart;

import lombok.Data;

/**
 * 今日按都道府县聚合的场次（dashboard 横向条形图）。
 *
 * <p>{@code regionName} 是 {@code areas.parent_id} 关联到的"地方"名（如
 * "関東地方"），方便前端按区域着色或分组。
 */
@Data
public class PrefectureShowtimeStatistics {
  Integer prefectureId;
  String prefectureName;
  String regionName;
  Long showtimeCount;
}
