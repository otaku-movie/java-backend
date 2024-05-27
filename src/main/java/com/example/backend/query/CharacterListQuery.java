package com.example.backend.query;

import lombok.Data;

import java.util.List;

@Data
public class CharacterListQuery {
  private Integer page;
  private Integer pageSize;
  private String name;
  private List<Integer> id;

  public CharacterListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
  }
}