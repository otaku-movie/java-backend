package com.example.backend.response;

import lombok.Data;

/**
 * 用户通过所有已绑定角色合并后的按钮权限（只读，用于「权限预览」）。
 */
@Data
public class UserEffectiveButtonResponse {
  private Integer id;
  private String name;
  private String i18nKey;
  private String apiCode;
}
