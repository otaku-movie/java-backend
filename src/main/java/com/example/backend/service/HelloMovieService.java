package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.HelloMovie;
import com.example.backend.entity.MovieTagTags;
import com.example.backend.mapper.HelloMovieMapper;
import com.example.backend.mapper.MovieTagTagsMapper;
import org.springframework.stereotype.Service;

/**
* @author last order
* @description 针对表【api】的数据库操作Service
* @createDate 2024-05-24 17:37:24
*/

@Service
public class HelloMovieService extends ServiceImpl<HelloMovieMapper, HelloMovie> {

}
