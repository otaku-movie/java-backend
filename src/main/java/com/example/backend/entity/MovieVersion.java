/*
 * @Author: last order 2495713984@qq.com
 * @Date: 2026-01-10 08:46:31
 * @LastEditors: last order 2495713984@qq.com
 * @LastEditTime: 2026-01-10 08:51:49
 * @FilePath: \movie\java-backend\src\main\java\com\example\backend\entity\MovieVersion.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电影版本表（配音版本管理）
 */
@TableName(value = "movie_version")
@Data
public class MovieVersion implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 电影ID
     */
    @TableField("movie_id")
    private Integer movieId;

    /**
     * 配音版本ID（字典：原版、中文配音、日语配音等）
     */
    @TableField("dubbing_version_id")
    private Integer dubbingVersionId;

    /**
     * 上映开始日期（仅配音版需要）
     */
    @TableField("start_date")
    private String startDate;

    /**
     * 上映结束日期（仅配音版需要）
     */
    @TableField("end_date")
    private String endDate;

    /**
     * 语言（字典ID）
     */
    @TableField("language_id")
    private Integer languageId;

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
