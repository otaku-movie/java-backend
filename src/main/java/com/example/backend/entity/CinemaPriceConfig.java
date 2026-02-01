package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 影院票价配置：3D 加价（2D 无加价，直接使用票种价）
 */
@Data
@TableName("cinema_price_config")
public class CinemaPriceConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("cinema_id")
    private Integer cinemaId;
    @TableField("dimension_type")
    private Integer dimensionType;
    @TableField("surcharge")
    private BigDecimal surcharge;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @JsonIgnore
    @TableLogic
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;
}
