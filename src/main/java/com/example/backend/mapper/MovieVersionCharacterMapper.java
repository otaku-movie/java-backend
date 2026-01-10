package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieVersionCharacter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电影版本角色关联Mapper
 */
@Mapper
public interface MovieVersionCharacterMapper extends BaseMapper<MovieVersionCharacter> {
}
