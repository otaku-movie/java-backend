package com.example.backend.query;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSaveQuery {
  Integer id;
  String cover;
  @NotEmpty(message = "{validator.saveUser.name.required}")
  String name;
  String password;
  @NotEmpty(message = "{validator.saveUser.email.required}")
  @Email(message = "{validator.saveUser.email.required}")
  String email;

  @NotEmpty(message = "{validator.saveUser.code.required}")
  @Size(min = 6, max = 6, message = "{validator.saveUser.code.length}")
  String code;

  @NotEmpty(message = "{validator.saveUser.token.required}")
  String token;
}
