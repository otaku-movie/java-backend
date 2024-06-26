package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.MovieSpec;
import com.example.backend.entity.MovieStaff;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MovieStaffMapper extends BaseMapper<MovieStaff> {
  void deleteStaff(Integer id);
}