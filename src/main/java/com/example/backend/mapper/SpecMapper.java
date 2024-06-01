package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.CinemaSpec;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;


@Mapper
public interface SpecMapper extends BaseMapper<CinemaSpec> {

}