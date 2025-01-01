package com.example.backend.response.comment;

import lombok.Data;

@Data
public class CommentReactionData {
  int likeCount;
  int dislikeCount;
  boolean like;
  boolean dislike;
}
