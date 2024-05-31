package com.example.backend.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
class Role {
  Integer id;
  String name;
}


@Data
public class UserListResponse extends User {
  List<Role> roles;
}
