package com.example.backend.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色响应对象
 */
@Data
public class CharacterResponse implements Serializable {
    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色原名
     */
    private String originalName;

    /**
     * 角色封面
     */
    private String cover;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 配音演员列表
     */
    private List<Staff> staff;
}
