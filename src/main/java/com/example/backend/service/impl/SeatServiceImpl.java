package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Api;
import com.example.backend.entity.Seat;
import com.example.backend.mapper.ApiMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.service.ApiService;
import com.example.backend.service.SeatService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【api】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat>
    implements SeatService {
}




