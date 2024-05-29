package com.example.backend.response;

import lombok.Data;

@Data
public class Button {
  Integer id;
  String i18n_key;
  String name;
  String code;
  Integer api_id;
  String api_name;
  String api_path;
  Boolean checked;
}