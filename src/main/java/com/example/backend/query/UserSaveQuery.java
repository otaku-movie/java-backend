package com.example.backend.query;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
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

  /** platform / chain / cinema，默认 platform */
  String dataScope;

  /** data_scope=chain 时必填 */
  Integer brandId;

  /** data_scope=cinema 时绑定的影院 ID */
  List<Integer> cinemaIds;
}
