package com.example.backend.response;

import lombok.Data;

import java.util.List;


@Data
public class AreaResponse {
  private Integer id;
  private String name;
  private String nameKana;
  private Integer parentId;
  private List<AreaResponse> children;
}