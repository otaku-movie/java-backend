package com.example.backend.response;

import com.example.backend.entity.Staff;
import lombok.Data;

import java.util.List;


@Data
public class MovieCharacterResponse {
  List<Staff> staff;
  Integer id;
  String name;
}