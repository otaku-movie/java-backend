package com.example.backend.service;

import com.example.backend.entity.BenefitUserFeedback;
import com.example.backend.mapper.BenefitUserFeedbackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 特典反馈异步落库（审计 / 运营列表），失败不影响用户侧成功提示。
 */
@Service
public class BenefitFeedbackArchiver {
  private static final Logger log = LoggerFactory.getLogger(BenefitFeedbackArchiver.class);

  @Autowired
  private BenefitUserFeedbackMapper benefitUserFeedbackMapper;

  @Async
  public void archive(BenefitUserFeedback row) {
    if (row == null) return;
    try {
      benefitUserFeedbackMapper.insert(row);
    } catch (DuplicateKeyException dup) {
      // 幂等：唯一索引已存在
    } catch (Exception e) {
      log.warn("benefit feedback archive failed benefitId={} cinemaId={} userId={}",
        row.getBenefitId(), row.getCinemaId(), row.getUserId(), e);
    }
  }
}
