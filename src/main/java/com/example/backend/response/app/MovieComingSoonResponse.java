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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date earliestShowTime;  // 最早场次时间
}
