package com.example.backend.response.cinema;

import lombok.Data;

@Data
public class MovieShowingResponse {
  Integer id;
  String name;
  String poster;
  Integer time;
  String levelName;
}
