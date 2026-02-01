package com.example.backend.query.dict;

public class DictListQuery {
  private Integer page;
  private Integer pageSize;
  private Integer id;
  private String name;
  private String code;

  public DictListQuery() {
    this.page = 1;
    this.pageSize = 10;
  }

  public Integer getPage() { return page; }
  public void setPage(Integer page) { this.page = page; }
  public Integer getPageSize() { return pageSize; }
  public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
}