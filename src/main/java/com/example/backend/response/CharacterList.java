package com.example.backend.response;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
class  Staff {
  private Integer id;
  private String name;
}

@Data
public class CharacterList {
  private Integer id;
  private String cover;
  private String name;
  private String original_name;
  private String description;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date create_time;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date update_time;

  private List<Staff> staff;
}
