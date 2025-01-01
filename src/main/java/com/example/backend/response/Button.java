package com.example.backend.response;

import lombok.Data;

@Data
public class Button {
  Integer id;
  String i18nKey;
  String name;
  String apiCode;
  Boolean checked;
}