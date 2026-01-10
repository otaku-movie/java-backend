package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电影版本角色关联表
 */
@TableName(value = "movie_version_character")
@Data
public class MovieVersionCharacter implements Serializable {
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
