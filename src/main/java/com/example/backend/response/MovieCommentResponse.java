package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MovieCommentResponse {
  Integer id;
  String content;
  Integer comment_user_id;
  Integer movie_id;
  Boolean like;
  Boolean unlike;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date create_time;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date update_time;
}
