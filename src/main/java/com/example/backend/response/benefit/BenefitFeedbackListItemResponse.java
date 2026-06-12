package com.example.backend.response.benefit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BenefitFeedbackListItemResponse {
  private Integer id;
  private Integer userId;
  private Integer cinemaId;
  private String cinemaName;
  private Integer benefitId;
  private String benefitName;
  private Integer feedbackType;
  /** 后台是否已读：0=未读 1=已读 */
  private Integer isRead;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date createTime;
}
