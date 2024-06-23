package com.example.backend.response;
import lombok.Data;

import java.util.List;

@Data
class CinemaSpec {
  Integer id;
  String name;
  Integer plus_price;
}

@Data
public class CinemaResponse {
  Integer id;
  String name;
  String description;
  String address;
  String tel;
  String home_page;
  Integer max_select_seat_count;
  Integer theater_count;
  List<CinemaSpec> spec;
}
