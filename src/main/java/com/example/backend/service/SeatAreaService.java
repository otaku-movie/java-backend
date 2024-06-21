package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Seat;
import com.example.backend.entity.SeatAisle;
import com.example.backend.entity.SeatArea;
import com.example.backend.mapper.SeatAisleMapper;
import com.example.backend.mapper.SeatAreaMapper;
import com.example.backend.mapper.SeatMapper;
import com.example.backend.query.SaveSeatQuery;
import com.example.backend.query.SeatAreaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatAreaService extends ServiceImpl<SeatAreaMapper, SeatArea> {
}
