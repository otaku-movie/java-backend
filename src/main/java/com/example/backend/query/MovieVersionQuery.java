package com.example.backend.query;

import lombok.Data;

import java.util.List;

/**
 * 电影版本查询对象
 */
@Data
public class MovieVersionQuery {
    /**
     * 版本ID（更新时使用）
     */
    private Integer id;
    
    /**
     * 版本代码（字典：原版、中文配音、日语配音等）
     */
    private Integer versionCode;
    
    /**
     * 开始日期
     */
    private String startDate;
    
    /**
     * 结束日期
     */
    private String endDate;
    
    /**
     * 语言ID
     */
    private Integer languageId;
    
    /**
     * 角色列表
     */
    private List<VersionCharacterQuery> characters;
}
