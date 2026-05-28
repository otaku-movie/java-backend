package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** C 端登录 / 注册返回，不含数据范围字段 */
@Data
public class AppLoginResponse {
  private Integer id;
  private String cover;
  private String name;
  private String email;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date createTime;

  /** 旧版 App 读取 token；新版本等同 accessToken，暂保留兼容。 */
  private String token;

  private String accessToken;

  private String refreshToken;

  private Long accessExpiresIn;

  private Long refreshExpiresIn;
}
