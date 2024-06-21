package com.example.backend.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TheaterHallSaveQuery {
  @NotNull(message = "{validator.saveTheaterHall.cinemaId.required}")
  private Integer cinemaId;
  @NotNull(message = "{validator.saveTheaterHall.cinemaSpecId.required}")
  private Integer cinemaSpecId;
  private Integer id;
  @NotEmpty(message = "{validator.saveTheaterHall.name.required}")
  private String name;

  @NotNull(message = "{validator.saveTheaterHall.rowCount.required}")
  private Integer rowCount;

  @NotNull(message = "{validator.saveTheaterHall.columnCount.required}")
  private Integer columnCount;
}
