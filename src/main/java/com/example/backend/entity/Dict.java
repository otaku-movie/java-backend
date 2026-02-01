package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict")
public class Dict {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("name")
  private String name;

  @TableField("code")
  private String code;
}