package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public  class CharacterSaveQuery {
  Integer id;
  @NotNull
  String name;

  @NotNull
  String description;

  @NotNull
  Integer movieId;

  @NotNull
  List<Integer> staffId;
}
