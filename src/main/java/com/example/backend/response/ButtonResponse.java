package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
class Button {
  Integer id;
  String name;
  String code;
  Integer api_id;
  String api_name;
  String api_path;
  Boolean checked;
}

@Data
public class ButtonResponse {
  Integer id;
  String name;
  String path;
  String path_name;
  Integer parent_id;
  Boolean show;
  Boolean checked;
  List<ButtonResponse> children;
  List<Button> button;
}
