package com.example.backend.response;

import lombok.Data;

import java.util.List;



@Data
public class ButtonResponse {
  Integer id;
  String name;
  String i18n_key;
  String path;
  String path_name;
  Integer parent_id;
  Boolean show;
  Boolean checked;
  List<ButtonResponse> children;
  List<Button> button;
}
