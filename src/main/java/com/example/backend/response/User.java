package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class User {
  Integer id;
  String cover;
  String name;
  String email;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date create_time;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date update_time;
}