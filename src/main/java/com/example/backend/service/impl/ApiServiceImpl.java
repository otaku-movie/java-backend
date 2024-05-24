package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Api;
import com.example.backend.mapper.ApiMapper;
import com.example.backend.service.ApiService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【api】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class ApiServiceImpl extends ServiceImpl<ApiMapper, Api>
    implements ApiService {
}




