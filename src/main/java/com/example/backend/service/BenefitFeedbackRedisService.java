package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * 特典「已领完」用户反馈：Redis 主路径（SET 去重 + 计数 + 阈值标志），MySQL 异步归档。
 */
@Component
public class BenefitFeedbackRedisService {

  @Autowired(required = false)
  private RedisTemplate<String, String> redisTemplate;

  @Value("${benefit.feedback.sold-out-threshold:3}")
  private int soldOutThreshold;

  @Value("${benefit.feedback.window-hours:24}")
  private int windowHours;

  private int ttlSeconds() {
    return Math.max(1, windowHours) * 3600;
  }

  private static String usersKey(int benefitId, int cinemaId) {
    return "benefit:feedback:users:" + benefitId + ":" + cinemaId;
  }

  private static String countKey(int benefitId, int cinemaId) {
    return "benefit:feedback:count:" + benefitId + ":" + cinemaId;
  }

  private static String soldOutKey(int benefitId, int cinemaId) {
    return "benefit:feedback:sold_out:" + benefitId + ":" + cinemaId;
  }

  public boolean isAvailable() {
    return redisTemplate != null;
  }

  /** 记录一次反馈；返回当前窗口内去重用户数（含本次新增与否） */
  public int recordFeedback(int benefitId, int cinemaId, int userId) {
    if (!isAvailable()) return 0;
    try {
      String uk = usersKey(benefitId, cinemaId);
      String ck = countKey(benefitId, cinemaId);
      String sk = soldOutKey(benefitId, cinemaId);
      int ttl = ttlSeconds();
      Duration dur = Duration.ofSeconds(ttl);

      Long added = redisTemplate.opsForSet().add(uk, String.valueOf(userId));
      redisTemplate.expire(uk, dur);

      long card = Objects.requireNonNullElse(redisTemplate.opsForSet().size(uk), 0L);
      redisTemplate.opsForValue().set(ck, String.valueOf(card), dur);

      if (card >= soldOutThreshold) {
        redisTemplate.opsForValue().set(sk, "1", dur);
      } else {
        redisTemplate.delete(sk);
      }
      return (int) card;
    } catch (Exception e) {
      return 0;
    }
  }

  public int getFeedbackCount(int benefitId, int cinemaId) {
    if (!isAvailable()) return 0;
    try {
      String v = redisTemplate.opsForValue().get(countKey(benefitId, cinemaId));
      if (v != null) {
        return Integer.parseInt(v);
      }
      Long sc = redisTemplate.opsForSet().size(usersKey(benefitId, cinemaId));
      int card = sc != null ? sc.intValue() : 0;
      if (card > 0) {
        redisTemplate.opsForValue().set(countKey(benefitId, cinemaId), String.valueOf(card), Duration.ofSeconds(ttlSeconds()));
      }
      return card;
    } catch (Exception e) {
      return 0;
    }
  }

  public boolean isSoldOutByFeedback(int benefitId, int cinemaId) {
    if (!isAvailable()) return false;
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(soldOutKey(benefitId, cinemaId)));
    } catch (Exception e) {
      return false;
    }
  }

  public boolean hasUserSubmitted(int benefitId, int cinemaId, int userId) {
    if (!isAvailable()) return false;
    try {
      return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(usersKey(benefitId, cinemaId), String.valueOf(userId)));
    } catch (Exception e) {
      return false;
    }
  }

  public void resetKeys(int benefitId, int cinemaId) {
    if (!isAvailable()) return;
    try {
      redisTemplate.delete(usersKey(benefitId, cinemaId));
      redisTemplate.delete(countKey(benefitId, cinemaId));
      redisTemplate.delete(soldOutKey(benefitId, cinemaId));
    } catch (Exception ignored) { }
  }
}
