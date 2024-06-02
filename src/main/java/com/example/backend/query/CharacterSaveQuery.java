package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public  class CharacterSaveQuery {
  Integer id;
  @NotNull
  String name;
  String cover;
  String originalName;

  @NotNull
  String description;

  @NotNull
  List<Integer> staffId;
}
