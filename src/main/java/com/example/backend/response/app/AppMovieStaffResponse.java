package com.example.backend.response.app;

import com.example.backend.entity.Position;
import lombok.Data;

import java.util.List;

@Data
public class AppMovieStaffResponse {
  Integer id;
  String name;
  String avatar;
  List<Position> position;
}
