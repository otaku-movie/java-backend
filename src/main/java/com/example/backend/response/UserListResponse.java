package com.example.backend.response;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class UserListResponse extends User {
  /**
   * 列表中的角色摘要；须为 static，供 MyBatis {@code ofType} 反射实例化。
   */
  @Data
  public static class Role {
    Integer id;
    String name;
  }

  List<Role> roles;

  @JsonIgnore private String cinemaIdsCsv;

  private List<Integer> cinemaIds = new ArrayList<>();

  public void setCinemaIdsCsv(String csv) {
    this.cinemaIdsCsv = csv;
    if (csv == null || csv.isBlank()) {
      this.cinemaIds = new ArrayList<>();
      return;
    }
    this.cinemaIds =
        Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toCollection(ArrayList::new));
  }

  /** PostgreSQL {@code json_agg} 结果字符串，由 MyBatis 写入后解析为 {@link #cinemaNames} */
  @JsonIgnore
  @Setter(AccessLevel.NONE)
  private String cinemaNamesJson;

  @Setter(AccessLevel.NONE)
  private List<String> cinemaNames = new ArrayList<>();

  public void setCinemaNamesJson(String json) {
    this.cinemaNamesJson = json;
    if (json == null || json.isBlank()) {
      this.cinemaNames = new ArrayList<>();
      return;
    }
    try {
      this.cinemaNames = JSON.parseArray(json, String.class);
    } catch (Exception e) {
      this.cinemaNames = new ArrayList<>();
    }
  }
}
