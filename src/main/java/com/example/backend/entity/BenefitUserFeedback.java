package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("benefit_user_feedback")
public class BenefitUserFeedback {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("user_id")
  private Integer userId;

  @TableField("cinema_id")
  private Integer cinemaId;

  @TableField("benefit_id")
  private Integer benefitId;

  /** 1=已领完 */
  @TableField("feedback_type")
  private Integer feedbackType;

  @JsonIgnore
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonIgnore
  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
