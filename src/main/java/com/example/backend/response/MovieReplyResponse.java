package com.example.backend.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MovieReplyResponse {
  Integer id;
  String content;
  Integer comment_user_id;
  Integer reply_user_id;
  String comment_user_name;
  String reply_user_name;
  Integer movie_comment_id;
  Boolean like;
  Boolean unlike;
  Integer like_count;
  Integer unlike_count;
  Integer reply_count;
  String parent_reply_id;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date create_time;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date update_time;
}
