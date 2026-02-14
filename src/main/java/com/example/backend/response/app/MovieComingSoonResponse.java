package com.example.backend.response.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MovieComingSoonResponse {
    private Integer id;
    private String name;
    private String cover;
    private String startDate;
    private Integer time;
    private String originalName;
    private String description;
    private Integer levelId;
    private Integer status;
    private Integer deleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    // 新增字段
    private String levelName;  // 分级名称
    private Integer presaleShowTimeCount;  // 预售场次数量

    /** 关联的预售券 id，有则可在 C 端跳转预售券详情 */
    private Integer presaleId;
    /** 是否有预售券（presale 表存在且未删除） */
    private Boolean hasPresaleTicket;
    /** 该预售券是否含特典（任一规格 bonus_title 非空或 bonus_included=true） */
    private Boolean hasBonus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date earliestShowTime;  // 最早场次时间
}
