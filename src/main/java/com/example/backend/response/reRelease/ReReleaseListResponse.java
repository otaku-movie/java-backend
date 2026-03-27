package com.example.backend.response.reRelease;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ReReleaseListResponse {
  Integer id;
  Integer movieId;
  String name;
  String cover;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  Date startDate;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  Date endDate;
  /** 1=启用 0=停用 */
  Integer status;
  /** 特殊版本说明/备注（可为空） */
  String versionInfo;
  /** 可选：覆盖展示名/海报（为空则复用 movie 字段） */
  String displayNameOverride;
  String posterOverride;

  /** 可选：覆盖片长（分钟，为空则复用 movie.time） */
  Integer timeOverride;
}
