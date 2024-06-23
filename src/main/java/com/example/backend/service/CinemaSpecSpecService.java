package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.CinemaSpecSpec;
import com.example.backend.entity.SelectSeat;
import com.example.backend.entity.TheaterHall;
import com.example.backend.mapper.*;
import com.example.backend.response.SeatListResponse;
import com.example.backend.response.SeatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author last order
* @description 针对表【api】的数据库操作Service
* @createDate 2024-05-24 17:37:24
*/

@Service
public class CinemaSpecSpecService extends ServiceImpl<CinemaSpecSpecMapper, CinemaSpecSpec> {
}
