package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.query.UserListQuery;
import com.example.backend.response.UserListResponse;
import com.example.backend.response.chart.StatisticsUserCount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {
  IPage<UserListResponse> userList(UserListQuery query, IPage<UserListResponse> page);
  List<Role> userRole(Integer id);

  List<StatisticsUserCount> StatisticsOfDailyRegisteredUsers();
}