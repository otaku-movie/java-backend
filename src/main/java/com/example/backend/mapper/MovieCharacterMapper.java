package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieCharacter;
import com.example.backend.entity.MovieSpec;
import com.example.backend.entity.MovieStaff;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MovieCharacterMapper extends BaseMapper<MovieCharacter> {

}