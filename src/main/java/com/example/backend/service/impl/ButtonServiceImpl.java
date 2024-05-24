package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Button;
import com.example.backend.entity.TheaterHall;
import com.example.backend.mapper.ButtonMapper;
import com.example.backend.service.ButtonService;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【button】的数据库操作Service实现
* @createDate 2024-05-24 17:37:24
*/
@Service
public class ButtonServiceImpl extends ServiceImpl<ButtonMapper, Button>
    implements ButtonService {

}




