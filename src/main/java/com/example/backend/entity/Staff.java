package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName position
 */
@TableName(value ="staff")
@Data
public class Staff implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    @TableField(value = "cover")
    private String cover;

    @TableField(value = "original_name")
    private String originalName;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    /**
     * 
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT)
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}