package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieVersionCharacterStaff;
import com.example.backend.response.CharacterResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电影版本角色配音演员关联Mapper
 */
@Mapper
public interface MovieVersionCharacterStaffMapper extends BaseMapper<MovieVersionCharacterStaff> {
    /**
     * 获取指定版本的角色列表（包含配音演员）
     */
    List<CharacterResponse> getVersionCharacters(@Param("movieVersionId") Integer movieVersionId);
}
