package com.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieNowShowingStatMapper {

  /** 全量重建正在上映统计（定时任务调用）。 */
  int rebuildAll();
}
