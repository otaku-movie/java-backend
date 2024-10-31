package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MovieCommentResponse {
  Integer id;
  String content;
  Integer comment_user_id;
  String comment_user_name;
  String comment_user_avatar;
  Integer movie_id;

  Boolean like = false;
  Boolean unlike = false;
  Integer like_count;
  Integer unlike_count;
  Integer reply_count;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date create_time;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date update_time;
}
