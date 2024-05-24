package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Role;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.service.RoleService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【role】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService {

}




