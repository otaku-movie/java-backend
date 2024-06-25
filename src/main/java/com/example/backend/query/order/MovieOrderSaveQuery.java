package com.example.backend.query.order;

import com.example.backend.query.SeatGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MovieOrderSaveQuery {
  // 场次 id
  @NotNull
  Integer movieShowTimeId;
  @NotNull
  List<SeatGroup> seat;
}
