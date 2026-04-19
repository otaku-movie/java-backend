package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/** 管理后台登录返回，含数据范围（见 RLS 设计文档 7.2） */
@Data
public class AdminLoginResponse {
  private Integer id;
  private String cover;
  private String name;
  private String email;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date createTime;

  private String token;

  /** platform / chain / cinema */
  private String dataScope;
  /** chain 级别时的品牌 ID */
  private Integer brandId;
  /** cinema 级别时绑定的影院 ID 列表 */
  private List<Integer> cinemaIds;
}
