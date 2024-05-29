package com.example.backend.response;

import lombok.Data;

import java.util.List;

@Data
public class MovieStaffResponse {
  List<Staff> staff;
  Integer id;
  String name;
}
