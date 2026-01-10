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
     * 配音版本ID
     */
    private Integer dubbingVersionId;
    
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
