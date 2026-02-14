package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 定价规则：活动内的一条规则，定义适用人群、票价、匹配优先级。
 * 最终总价 = 基础价(由规则或固定价或抵扣决定) + 场次规格补价(surcharge)。
 */
@Data
@TableName("pricing_rule")
public class PricingRule {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("cinema_id")
  private Integer cinemaId;

  /** 适用人群，dict_item.code 或与票种对应 */
  @TableField("audience_type")
  private Integer audienceType;

  /** 该规则对应票价(基础价) */
  @TableField("value")
  private BigDecimal value;

  /** 匹配优先级，数值越小越优先 */
  @TableField("priority")
  private Integer priority;

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
