package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;

/** 后台用户管理：编辑弹窗拉取 */
@Data
public class AdminUserDetailResponse {
  private Integer id;
  private String cover;
  private String name;
  private String email;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date createTime;

  private String dataScope;
  private Integer brandId;
  private List<Integer> cinemaIds;

  /** 院线级：品牌名称（顶栏展示） */
  private String brandName;

  /** 影院级：与 {@link #cinemaIds} 顺序一致的影院名称 */
  private List<String> cinemaNames;
}
