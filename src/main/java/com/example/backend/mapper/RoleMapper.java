package com.example.backend.mapper;

import com.example.backend.entity.Role;
import com.example.backend.entity.TheaterHall;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.response.RolePermissionButton;

import java.util.List;

/**
* @author last order
* @description 针对表【role】的数据库操作Mapper
* @createDate 2024-05-24 17:37:24
* @Entity com.example.backend.entity.TheaterHall.Role
*/
public interface RoleMapper extends BaseMapper<Role> {
  List permissionList(Integer id);
  List rolePermission(Integer id);
  List<RolePermissionButton> rolePermissionButton(Integer roleId);
}




