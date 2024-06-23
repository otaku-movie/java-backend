package com.example.backend.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TheaterHallList {
  Integer id;
  String name;
  Integer row_count;
  Integer column_count;
  Integer cinema_id;
  Integer cinema_spec_id;
  String cinema_spec_name;
  Integer seat_count;
}
