package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName character
 */
@TableName(value ="character")
@Data
public class Character implements Serializable {
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

    /**
     * 
     */
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 
     */
    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}