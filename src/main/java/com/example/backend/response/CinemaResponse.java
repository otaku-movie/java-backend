package com.example.backend.response;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class CinemaResponse {
  Integer id;
  String name;
  String description;
  String address;
  String tel;
  String homePage;
  Integer maxSelectSeatCount;
  Integer theaterCount;
  Integer brandId;
  String brandName;
  List<Spec> spec;
  private Integer regionId;
  private Integer prefectureId;
  private Integer cityId;
  private String fullAddress;
}
