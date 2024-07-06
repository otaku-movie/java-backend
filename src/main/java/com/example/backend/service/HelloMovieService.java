package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Api;
import com.example.backend.entity.MovieTagTags;
import com.example.backend.entity.Seat;
import com.example.backend.mapper.MovieTagTagsMapper;
import com.example.backend.mapper.SeatMapper;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【api】的数据库操作Service
* @createDate 2024-05-24 17:37:24
*/

@Service
public class MovieTagTagsService extends ServiceImpl<MovieTagTagsMapper, MovieTagTags> {

}
