package com.example.backend.query;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.MovieCharacter;
import com.example.backend.entity.MovieStaff;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SaveMovieQuery {
  private Integer id;
  private String cover;
  @NotNull
  @NotEmpty
  private String name;
  @NotEmpty
  private String description;
  private Integer levelId;
  private String startDate;
  private String endDate;
  private Integer status;
  private Integer time;
  private String HomePage;
  private String originalName;
  private List<Integer> spec;
  private List<MovieStaffQuery> staffList;
  private List<MovieCharacterQuery> characterList;
  private List<Integer> tags;
  private List<HelloMovie> helloMovie;
}