package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName position
 */
@TableName(value ="movie_staff")
@Data
public class MovieStaff implements Serializable {
    @TableField("movie_id")
    private  Integer movieId;

    @TableField("staff_id")
    private  Integer staffId;

    @TableField("position_id")
    private  Integer positionId;

    /**
     * 
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}