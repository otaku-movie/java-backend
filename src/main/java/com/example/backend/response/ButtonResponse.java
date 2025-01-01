package com.example.backend.response;

import lombok.Data;

import java.util.List;



@Data
public class ButtonResponse {
  Integer id;
  String i18nKey;
  String path;
  String pathName;
  Integer parentId;
  Boolean show;
  Boolean checked;
  List<ButtonResponse> children;
  List<Button> button;
}
