package com.example.backend.query;

import lombok.Data;

import java.util.List;

/**
 * 版本角色查询对象
 */
@Data
public class VersionCharacterQuery {
    /**
     * 角色ID
     */
    private Integer id;
    
    /**
     * 演员ID列表
     */
    private List<Integer> staffIds;
}
