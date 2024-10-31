package com.example.backend.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public  class CharacterSaveQuery {
  Integer id;
  @NotEmpty(message = "{validator.saveCharacter.name.required}")
  String name;
  String cover;
  String originalName;

  @NotNull
  String description;

  List<Integer> staffId;
}
