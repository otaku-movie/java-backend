package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class User {
  Integer id;
  String cover;
  String name;
  String email;

  /** platform / chain / cinema，对应 users.data_scope */
  String dataScope;

  /** data_scope=chain 时对应 users.brand_id */
  Integer brandId;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date createTime;

  @JsonIgnore
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date updateTime;
}