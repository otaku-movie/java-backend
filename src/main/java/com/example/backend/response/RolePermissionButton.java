package com.example.backend.response;

import lombok.Data;

@Data
public class RolePermissionButton {
  Integer userId;
  Integer roleId;
  Integer buttonId;
  String buttonName;
  String apiCode;
}
