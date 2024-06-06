package com.example.backend.response;

import lombok.Data;

@Data
public class RolePermissionButton {
  Integer user_id;
  Integer role_id;
  Integer button_id;
  String button_name;
  String button_code;
  Integer api_id;
  String api_name;
  String api_code;
}
