package com.example.backend.response;

import lombok.Data;

import java.util.List;


@Data
public class AreaResponse {
  private Integer id;
  private String name;
  private String nameKana;
  /** 中文译名；为空时 C 端只显示日文 name */
  private String nameZh;
  /** 英文译名；为空时 C 端只显示日文 name */
  private String nameEn;
  private Integer parentId;
  private List<AreaResponse> children;
}