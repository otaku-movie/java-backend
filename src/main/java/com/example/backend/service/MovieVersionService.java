package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieVersion;
import com.example.backend.mapper.MovieVersionMapper;
import com.example.backend.response.movie.MovieVersionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 电影版本服务类
 */
@Service
public class MovieVersionService extends ServiceImpl<MovieVersionMapper, MovieVersion> {
    
    @Autowired
    private MovieVersionMapper movieVersionMapper;
    
    /**
     * 获取电影的所有版本信息（包含角色和演员）
     * 
     * @param movieId 电影ID
     * @return 电影版本列表
     */
    public List<MovieVersionResponse> getMovieVersions(Integer movieId) {
        return movieVersionMapper.getMovieVersions(movieId);
    }
}
