package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.backend.typeHandler.StringArrayTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@TableName(value = "presale", autoResultMap = true)
public class Presale {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("code")
  private String code;

  @TableField("title")
  private String title;

  /** dict_item.code，presaleDeliveryType：1=虚拟 2=实体 */
  @TableField("delivery_type")
  private Integer deliveryType;

  /** dict_item.code，presaleDiscountMode：1=固定 2=比例 */
  @TableField("discount_mode")
  private Integer discountMode;

  @TableField("price")
  private BigDecimal price;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("total_quantity")
  private Integer totalQuantity;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("launch_time")
  private Date launchTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("end_time")
  private Date endTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("usage_start")
  private Date usageStart;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField("usage_end")
  private Date usageEnd;

  @TableField("per_user_limit")
  private Integer perUserLimit;

  @TableField("movie_id")
  private Integer movieId;

  @TableField("pickup_notes")
  private String pickupNotes;

  @TableField("cover")
  private String cover;

  @TableField(value = "gallery", typeHandler = StringArrayTypeHandler.class)
  private List<String> gallery;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  @TableLogic
  @TableField(value = "deleted", fill = FieldFill.INSERT)
  private Integer deleted;
}
