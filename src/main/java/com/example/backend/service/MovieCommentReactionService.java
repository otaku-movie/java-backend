package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.MovieCommentReaction;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.mapper.MovieCommentReactionMapper;
import org.springframework.stereotype.Service;

@Service
public class MovieCommentReactionService extends ServiceImpl<MovieCommentReactionMapper, MovieCommentReaction>  {

}
