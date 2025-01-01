package com.example.backend.response.app;


import com.example.backend.response.Staff;
import com.example.backend.response.Spec;
import com.example.backend.response.movie.HelloMovie;
import com.example.backend.response.movie.Tags;
import lombok.Data;

import java.util.List;

@Data
public class NowMovieShowingResponse {
  Integer id;
  String name;
  String cover;
  List<Tags> tags;
  List<Spec> spec;
  String levelName;
  List<Staff> cast;
  List<HelloMovie> helloMovie;
  String startDate;
}
