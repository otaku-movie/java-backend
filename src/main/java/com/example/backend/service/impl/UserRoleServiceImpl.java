package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.UserRole;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【user_role】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService {

}




