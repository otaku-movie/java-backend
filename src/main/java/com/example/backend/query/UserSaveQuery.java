package com.example.backend.query;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSaveQuery {
  Integer id;
  String cover;
  @NotNull
  String name;
  @NotNull
  String password;
  @NotNull
  @Email
  String email;
}
