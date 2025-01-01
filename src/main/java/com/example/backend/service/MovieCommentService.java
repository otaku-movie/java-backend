package com.example.backend.service;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MovieComment;
import com.example.backend.entity.MovieCommentReaction;
import com.example.backend.entity.MovieReply;
import com.example.backend.enumerate.CommentEnumType;
import com.example.backend.enumerate.RedisType;
import com.example.backend.mapper.MovieCommentMapper;
import com.example.backend.mapper.MovieCommentReactionMapper;
import com.example.backend.mapper.MovieReplyMapper;
import com.example.backend.response.comment.CommentReactionData;
import com.example.backend.utils.Utils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import cn.dev33.satoken.stp.StpUtil;
import java.util.*;

@Service
public class MovieCommentService extends ServiceImpl<MovieCommentMapper, MovieComment> {
  @Resource
  RedisTemplate redisTemplate;

  @Autowired
  MovieCommentReactionService movieCommentReactionService;

  @Autowired
  MovieCommentReactionMapper movieCommentReactionMapper;

  @Autowired
  MovieReplyMapper movieReplyMapper;

  public CommentReactionData getRedisData(String type, Integer id) {
    // 获取 Redis 键前缀
    Map<String, String> redisKeys = getRedisKeys(type);
    String redisLikeCode = redisKeys.get("likeCode");
    String redisDislikeCode = redisKeys.get("dislikeCode");
    String redisLikeCountCode = redisKeys.get("likeCountCode");
    String redisDislikeCountCode = redisKeys.get("dislikeCountCode");

    // 初始化返回对象
    CommentReactionData reactionData = new CommentReactionData();
    reactionData.setLikeCount(0);
    reactionData.setDislikeCount(0);
    reactionData.setLike(false);
    reactionData.setDislike(false);

    // 获取当前用户的 ID（通过 SaToken 判断是否登录）
    String userId = null;
    if (StpUtil.isLogin()) {
      userId = String.valueOf(StpUtil.getLoginId());
    }

    // 获取 Redis 中点赞和点踩的数量
    String commentIdStr = String.valueOf(id);
    String likeCountStr = (String) redisTemplate.opsForHash().get(redisLikeCountCode, commentIdStr);
    String dislikeCountStr = (String) redisTemplate.opsForHash().get(redisDislikeCountCode, commentIdStr);

    // 解析 Redis 数据
    try {
      if (likeCountStr != null) {
        reactionData.setLikeCount(Integer.parseInt(likeCountStr));
      }
      if (dislikeCountStr != null) {
        reactionData.setDislikeCount(Integer.parseInt(dislikeCountStr));
      }
    } catch (NumberFormatException e) {
      System.err.println("Failed to parse like/dislike count from Redis: " + e.getMessage());
    }

    String keyName = type == CommentEnumType.comment.getCode() ? "commentId" : "replyId";
    // 如果用户已登录，检查其点赞和点踩状态
    if (userId != null) {
      // 构建 Redis 键
      String likeKey = String.format("%s:{" + keyName +":%d}", redisLikeCode, id);
      String dislikeKey = String.format("%s:{" + keyName + ":%d}", redisDislikeCode, id);

      // 从 Redis 中获取当前用户的点赞和点踩状态
      Boolean isLiked = redisTemplate.opsForHash().hasKey(likeKey, userId);
      Boolean isDisliked = redisTemplate.opsForHash().hasKey(dislikeKey, userId);

      // 设置点赞和点踩状态
      reactionData.setLike(Boolean.TRUE.equals(isLiked));
      reactionData.setDislike(Boolean.TRUE.equals(isDisliked));
    }

    return reactionData;
  }


  /**
   * 根据类型（comment 或 reply）生成 Redis 键的相关配置
   *
   * @param type 类型："comment" 或 "reply"
   * @return 包含所有相关 Redis 键的 Map
   */
  private Map<String, String> getRedisKeys(String type) {
    boolean isComment = type == CommentEnumType.comment.getCode();

    Map<String, String> redisKeys = new HashMap<>();
    redisKeys.put("likeCode", isComment ? RedisType.userMovieCommentLike.getCode() : RedisType.userMovieReplyLike.getCode());
    redisKeys.put("dislikeCode", isComment ? RedisType.userMovieCommentDislike.getCode() : RedisType.userMovieReplyDislike.getCode());
    redisKeys.put("likeCountCode", isComment ? RedisType.userMovieCommentLikeCount.getCode() : RedisType.userMovieReplyLikeCount.getCode());
    redisKeys.put("dislikeCountCode", isComment ? RedisType.userMovieCommentDislikeCount.getCode() : RedisType.userMovieReplyDislikeCount.getCode());

    return redisKeys;
  }

  public boolean toggleAction(String type, Integer commentId, boolean isLike) {
    boolean isComment = type == CommentEnumType.comment.getCode();
    Map<String, String> redisKeys = getRedisKeys(type);

    String redisLikeCode = redisKeys.get("likeCode");
    String redisDislikeCode = redisKeys.get("dislikeCode");
    String redisLikeCountCode = redisKeys.get("likeCountCode");
    String redisDislikeCountCode = redisKeys.get("dislikeCountCode");

    // 确定当前操作和对立操作的 Redis 键
    String actionCode = isLike ? redisLikeCode : redisDislikeCode;
    String oppositeActionCode = isLike ? redisDislikeCode : redisLikeCode;

    String userId = String.valueOf(Utils.getUserId());

    String commentIdStr = String.valueOf(commentId);


    String formatStr = String.format("{%s:%d}", isComment  ? "commentId" : "replyId", commentId);

    String actionKey = String.join(":", actionCode, formatStr);
    String oppositeActionKey = String.join(":", oppositeActionCode, formatStr);

    // 当前操作和对立操作的数量键
    String actionCountKey = isLike ? redisLikeCountCode : redisDislikeCountCode;
    String oppositeActionCountKey = isLike ? redisDislikeCountCode : redisLikeCountCode;

    // 如果 Redis 中没有当前操作或对立操作的数据，从数据库加载
    if (
      !redisTemplate.opsForHash().hasKey(actionKey, userId) &&
      !redisTemplate.opsForHash().hasKey(oppositeActionKey, userId)
    ) {
      // 从数据库加载评论反应数据
      loadCommentReactionsFromDatabase(type, commentId);
    }

    // 检查用户是否已经执行对立操作
    if (redisTemplate.opsForHash().hasKey(oppositeActionKey, userId)) {
      // 取消对立操作
      redisTemplate.opsForHash().delete(oppositeActionKey, userId);
      redisTemplate.opsForHash().increment(oppositeActionCountKey, commentIdStr, -1);
    }

    // 检查用户是否已经执行当前操作
    if (redisTemplate.opsForHash().hasKey(actionKey, userId)) {
      // 已经执行，取消当前操作
      redisTemplate.opsForHash().delete(actionKey, userId);
      redisTemplate.opsForHash().increment(actionCountKey, commentIdStr, -1);
      return false; // 返回 false 表示取消了操作
    }

    // 未执行，添加当前操作
    redisTemplate.opsForHash().put(actionKey, userId, String.valueOf(System.currentTimeMillis()));
    redisTemplate.opsForHash().increment(actionCountKey, String.valueOf(commentId), 1);
    return true; // 返回 true 表示成功执行操作
  }
  //  从数据库查询该评论的所有点赞和点踩并存储到redis
  private void loadCommentReactionsFromDatabase(String type, Integer commentId) {
    List<MovieCommentReaction> reactions = movieCommentReactionMapper.selectList(
      new QueryWrapper<MovieCommentReaction>().eq("id", commentId)
    );
    Boolean isComment = type == CommentEnumType.comment.getCode();

    // 遍历每个评论反应并将其加载到 Redis
    for (MovieCommentReaction reaction : reactions) {
//      String redisKey = String.format("movie:comment:reaction:{commentId:%d}", commentId);
      String userId = String.valueOf(reaction.getUserId());

      // 如果是点赞操作，存储在点赞 Redis 键中
      String actionKey = String.format(
        "%s:{%s:%d}",
        getRedisKeys(type).get("like"),
        isComment ? "commentId" : "replyId",
        commentId
      );
      if (reaction.getLike()) {
        redisTemplate.opsForHash().put(actionKey, userId, String.valueOf(reaction.getCreateTime().getTime()));
      }

      // 如果是点踩操作，存储在点踩 Redis 键中
      String oppositeActionKey = String.format(
        "%s:{%s:%d}",
        getRedisKeys(type).get("dislike"),
        isComment ? "commentId" : "replyId",
        commentId
      );
      if (reaction.getDislike()) {
        redisTemplate.opsForHash().put(oppositeActionKey, userId, String.valueOf(reaction.getCreateTime().getTime()));
      }

      // 更新点赞和点踩的计数
      String likeCountKey = getRedisKeys(type).get("likeCount");
      String dislikeCountKey = getRedisKeys(type).get("dislikeCount");

      if (reaction.getLike()) {
        redisTemplate.opsForHash().increment(likeCountKey, String.valueOf(commentId), 1);
      }
      if (reaction.getDislike()) {
        redisTemplate.opsForHash().increment(dislikeCountKey, String.valueOf(commentId), 1);
      }
    }
  }
  // 同步数量
  public void syncCommentLikeAndDislikeCount(String type) {
    String redisLikeCountCode = getRedisKeys(type).get("likeCountCode");
    String redisDislikeCountCode = getRedisKeys(type).get("dislikeCountCode");

    Map<Object, Object> likeCounts = redisTemplate.opsForHash().entries(redisLikeCountCode);
    Map<Object, Object> dislikeCounts = redisTemplate.opsForHash().entries(redisDislikeCountCode);

    // 获取对象的key，和值, 把likecount 和dislikeCounts的值存到一块 [likeCount, dislikeCount]
    Map<Integer, List<Integer>> countMap = new HashMap<>();

    likeCounts.forEach((key, value) -> {
      Integer commentId = Integer.parseInt(key.toString());
      Integer likeCount = Integer.parseInt(value.toString());
      countMap.put(commentId, new ArrayList<>(Arrays.asList(likeCount, 0)));
    });

    dislikeCounts.forEach((key, value) -> {
      Integer commentId = Integer.parseInt(key.toString());
      Integer dislikeCount = Integer.parseInt(value.toString());
      // 检查 countMap 中是否已经有这个 commentId。
      //如果没有，就创建一个新的 ArrayList，并初始化为 [0, 0]（0 个赞，0 个踩）。
      //如果已经存在，则直接返回现有的值。
      countMap.computeIfAbsent(commentId, k -> new ArrayList<>(Arrays.asList(0, 0)))
        // 将 countMap 中对应 commentId 的列表的第二个元素（索引 1）更新为 dislikeCount。
        .set(1, dislikeCount); // Update dislike count
    });

    if (type == CommentEnumType.comment.getCode()) {
      countMap.forEach((commentId, counts) -> {
        MovieComment movieComment = new MovieComment();

        movieComment.setId(commentId);
        movieComment.setLikeCount(counts.get(0));
        movieComment.setDislikeCount(counts.get(1));
        this.updateById(movieComment);
      });
    } else {
      countMap.forEach((commentId, counts) -> {
        MovieReply movieReply = new MovieReply();

        movieReply.setId(commentId);
        movieReply.setLikeCount(counts.get(0));
        movieReply.setDislikeCount(counts.get(1));
        movieReplyMapper.updateById(movieReply);
      });
    }

  }
  public void syncCommentLikeAndDislike() {
    this.syncComment();
    this.syncReply();
  }
  void syncComment () {
    // 同步评论数量
    this.syncCommentLikeAndDislikeCount(CommentEnumType.comment.getCode());
    // 同步评论的点赞和点踩数据
    String redisLikeCode = getRedisKeys(CommentEnumType.comment.getCode()).get("likeCode");
    String redisDislikeCode = getRedisKeys(CommentEnumType.comment.getCode()).get("dislikeCode");

    // 用于存储每个 commentId 的操作数据
    Map<String, List<Map<String, Object>>> commentDataMap = new HashMap<>();

    // 处理评论的点赞和点踩数据 从而同步到数据库
    processActionData(CommentEnumType.comment.getCode(), redisLikeCode, true, commentDataMap);
    processActionData(CommentEnumType.comment.getCode(), redisDislikeCode, false, commentDataMap);
    // 把数据同步到数据库
    this.syncDataBase(CommentEnumType.comment.getCode(), commentDataMap);
  }
  void syncReply() {
    // 同步回复数量
    this.syncCommentLikeAndDislikeCount(CommentEnumType.reply.getCode());

    // 同步评论的点赞和点踩数据
    String redisLikeCode = getRedisKeys(CommentEnumType.reply.getCode()).get("likeCode");
    String redisDislikeCode = getRedisKeys(CommentEnumType.reply.getCode()).get("dislikeCode");

    // 用于存储每个 commentId 的操作数据
    Map<String, List<Map<String, Object>>> commentDataMap = new HashMap<>();

    processActionData(CommentEnumType.reply.getCode(), redisLikeCode, true, commentDataMap);
    processActionData(CommentEnumType.reply.getCode(), redisDislikeCode, false, commentDataMap);
    this.syncDataBase(CommentEnumType.reply.getCode(), commentDataMap);
  }

  void syncDataBase (
    String type,
    Map<String, List<Map<String, Object>>> commentDataMap
    ) {
    // 清空表
    QueryWrapper movieCommentReactionQueryWrapper = new QueryWrapper<>();

    movieCommentReactionQueryWrapper.eq("type", type);

    movieCommentReactionService.remove(movieCommentReactionQueryWrapper);

    // 构建数据
    Collection<List<Map<String, Object>>> list = commentDataMap.values();

    List<MovieCommentReaction> sortedList = list.stream()
      .flatMap(Collection::stream) // 拉平所有评论，转换为流
      .map((item) -> {
        MovieCommentReaction movieCommentReaction = new MovieCommentReaction();

        // 确保类型转换正确

        if (type == CommentEnumType.comment.getCode()) {
          movieCommentReaction.setType(CommentEnumType.comment.getCode());
          movieCommentReaction.setMovieCommentId(Integer.parseInt((String) item.get("commentId")));
        } else {
          movieCommentReaction.setType(CommentEnumType.reply.getCode());
          movieCommentReaction.setMovieReplyId(Integer.parseInt((String) item.get("replyId")));
        }

        movieCommentReaction.setLike((Boolean) item.get("like")); // 将 like 转换为布尔值
        movieCommentReaction.setDislike((Boolean) item.get("dislike")); // 将 dislike 转换为布尔值
        movieCommentReaction.setUserId(Integer.parseInt((String) item.get("userId"))); // 将 userId 转换为整数


        Long createTime = Long.valueOf((String) item.get("createTime")); // 将 createTime 从 String 转为 Long
        movieCommentReaction.setCreateTime(new DateTime(createTime)); // 将 createTime 转为 DateTime 对象
        movieCommentReaction.setUpdateTime(new DateTime(createTime)); // 将 updateTime 转为 DateTime 对象

        return movieCommentReaction;
      })
      .collect(Collectors.toList());

    // 插入到数据库
    movieCommentReactionService.saveBatch(sortedList);
  }
  /**
   * 处理点赞或点踩数据
   *
   * @param redisCode       Redis 键前缀
   * @param isLike          是否是点赞
   * @param commentDataMap  存储按 commentId 分组的结果
   */
  private void processActionData(
    String type,
    String redisCode,
    boolean isLike,
    Map<String, List<Map<String, Object>>> commentDataMap
  ) {
    // 获取所有以该前缀开头的键
    String keyName = type == CommentEnumType.comment.getCode() ? "commentId" : "replyId";
    Set<String> keys = redisTemplate.keys(redisCode + ":{" + keyName + "*");

    if (keys != null && !keys.isEmpty()) {
      for (String key : keys) {
        // 解析 Redis 键
        Map<String, String> redisKeyMap = Utils.parseRedisKey(key);

        // 确保键中包含 commentId
        if (redisKeyMap.get(keyName) != null) {
          String commentId = redisKeyMap.get(keyName);

          // 获取用户操作记录（userId -> createTime）
          Map<Object, Object> userActions = redisTemplate.opsForHash().entries(key);

          // 获取该 commentId 的操作数据列表（如果不存在，则初始化一个新 List）
          List<Map<String, Object>> actionDataList = commentDataMap.getOrDefault(commentId, new ArrayList<>());

          for (Map.Entry<Object, Object> entry : userActions.entrySet()) {
            String userId = (String) entry.getKey();
            Map<String, Object> actionData = new HashMap<>();

            actionData.put(keyName, commentId);
            actionData.put("type", type);
            actionData.put("userId", userId);
            actionData.put("createTime", entry.getValue());

            // 设置默认值
            actionData.put("like", false); // 默认是 false
            actionData.put("dislike", false); // 默认是 false

            // 如果是点踩且该用户有点赞，则将 like 设置为 false
            if (!isLike) {
              actionData.put("dislike", true); // 设置点踩
            } else {
              actionData.put("like", true); // 点赞时，设置点赞为 true
            }

            // 将用户的操作数据加入到该 commentId 对应的 List 中
            actionDataList.add(actionData);
          }

          // 将 commentId 的操作数据列表保存到 commentDataMap 中
          commentDataMap.put(commentId, actionDataList);
        }
      }
    }
  }

}
