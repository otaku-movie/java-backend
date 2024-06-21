package com.example.backend.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.backend.enumerate.SeatType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaveSeatQuery {
  @NotEmpty(message = "{validator.saveSeat.theaterHallId.required}")

  Integer theaterHallId;
  List<SeatQuery> seat;
  List<SeatAreaQuery> area;
  List<SeatAisleQuery> aisle;
}
