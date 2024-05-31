package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleConfigQuery {

  @NotNull
  Integer id;
  @NotNull
  List<Integer> roleId;
}
