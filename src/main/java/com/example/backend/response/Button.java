package com.example.backend.response;

import lombok.Data;

@Data
public class Button {
  Integer id;
  String name;
  String code;
  Integer api_id;
  String api_name;
  String api_path;
  Boolean checked;
}