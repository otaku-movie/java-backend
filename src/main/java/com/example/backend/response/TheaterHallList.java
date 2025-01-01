package com.example.backend.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TheaterHallList {
  Integer id;
  String name;
  Integer rowCount;
  Integer columnCount;
  Integer cinemaId;
  Integer cinemaSpecId;
  String cinemaSpecName;
  Integer seatCount;
}
