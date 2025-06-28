package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName character
 */
@TableName(value ="areas")
@Data
public class Areas implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    @TableField(value = "name")
    private String name;

    @TableField(value = "name_kana")
    private String nameKana;

    @TableField(value = "parent_id")
    private Integer parentId;

//    @TableField(exist = false) // 表示这个字段数据库表里没有，不做映射
    @TableField(value = "create_time", fill = FieldFill.INSERT, exist = false)
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE, exist = false)
    private Date updateTime;

    /**
     * 
     */
    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT, exist = false)
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}