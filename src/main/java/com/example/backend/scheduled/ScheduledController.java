package com.example.backend.scheduled;

import com.example.backend.service.MovieCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledController {
  @Autowired
  MovieCommentService movieCommentService;


  // 每半小时同步 Redis 的评论点赞和点踩数据到数据库
  @Scheduled(cron = "0 0/30 * * * ?")
  public void syncCommentLikeAndDislike() {
    movieCommentService.syncCommentLikeAndDislike();
  }
}
