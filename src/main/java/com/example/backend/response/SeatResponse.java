package com.example.backend.response;

import com.example.backend.query.SeatAreaQuery;
import com.example.backend.query.SeatQuery;
import lombok.Data;

import java.util.List;


@Data
public class SeatResponse {
  Integer rowAxis;
  List<SeatListResponse> children;
}
