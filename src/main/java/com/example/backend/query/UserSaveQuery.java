package com.example.backend.query;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserSaveQuery {
  Integer id;
  String cover;
  @NotEmpty(message = "validator.saveUser.name.required")
  String name;
  @NotEmpty(message = "validator.saveUser.password.required")
  String password;
  @NotEmpty(message = "validator.saveUser.email.required")
  @Email(message = "validator.saveUser.email.required")
  String email;
}
