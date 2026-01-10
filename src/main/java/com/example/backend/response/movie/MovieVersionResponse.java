package com.example.backend.response.movie;

import com.example.backend.response.CharacterResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 电影版本响应对象
 */
@Data
public class MovieVersionResponse implements Serializable {
    /**
     * 版本ID
     */
    private Integer id;

    /**
     * 电影ID
     */
    private Integer movieId;

    /**
     * 配音版本ID
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
    private List<CharacterResponse> characters;
}
