package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_agreement_acceptance")
public class UserAgreementAcceptance {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("user_id")
  private Integer userId;

  @TableField("agreement_code")
  private String agreementCode;

  @TableField("agreement_version")
  private String agreementVersion;

  @TableField("language")
  private String language;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("accepted_at")
  private Date acceptedAt;

  @TableField("client")
  private String client;

  @TableField("device_id")
  private String deviceId;

  @TableField("ip")
  private String ip;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
