package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电影版本角色配音演员关联表
 */
@TableName(value = "movie_version_character_staff")
@Data
public class MovieVersionCharacterStaff implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 电影版本ID
     */
    @TableField("movie_version_id")
    private Integer movieVersionId;

    /**
     * 角色ID
     */
    @TableField("character_id")
    private Integer characterId;

    /**
     * 演员ID（配音演员）
     */
    @TableField("staff_id")
    private Integer staffId;

    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @JsonIgnore
    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
