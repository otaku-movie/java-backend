package com.example.backend.response.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Tags {
  Integer id;
  String name;

  // 译名列（name=日文原名）。仅供后端按语言选用，不直接对外序列化：
  // 接口仍只返回 id + name（name 已按 Accept-Language 填好对应语言）。
  @JsonIgnore
  String nameZh;

  @JsonIgnore
  String nameEn;
}
