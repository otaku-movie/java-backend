package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.SeatAisle;
import com.example.backend.entity.SeatArea;
import com.example.backend.mapper.SeatAisleMapper;
import com.example.backend.mapper.SeatAreaMapper;
import org.springframework.stereotype.Service;

@Service
public class SeatAisleService extends ServiceImpl<SeatAisleMapper, SeatAisle> {
}
