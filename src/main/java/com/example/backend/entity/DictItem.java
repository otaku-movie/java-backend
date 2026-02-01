package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_item")
public class DictItem {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("name")
  private String name;

  @TableField("code")
  private Integer code;

  @TableField("dict_id")
  private Integer dictId;

//  @TableField("description")
//  String description;
}