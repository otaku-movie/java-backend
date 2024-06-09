package com.example.backend.response;

import lombok.Data;

@Data
public class Button {
  Integer id;
  String i18n_key;
  String name;
  String api_code;
  Boolean checked;
}