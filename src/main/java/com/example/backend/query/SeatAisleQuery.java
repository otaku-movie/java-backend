package com.example.backend.query;

import com.example.backend.enumerate.SeatType;
import lombok.Data;

@Data
public class SeatAisleQuery {
  Integer id;
  String type;
  Integer start;
}
