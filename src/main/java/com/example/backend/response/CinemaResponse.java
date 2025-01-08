package com.example.backend.response;
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
}
