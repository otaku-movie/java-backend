package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.UserOAuthBinding;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserOAuthBindingMapper extends BaseMapper<UserOAuthBinding> {
}
