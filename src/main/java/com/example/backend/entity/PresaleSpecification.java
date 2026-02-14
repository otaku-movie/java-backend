package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.example.backend.typeHandler.JsonbTypeHandler;
import com.example.backend.typeHandler.StringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "presale_specification", autoResultMap = true)
public class PresaleSpecification {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  @TableField("presale_id")
  private Integer presaleId;

  @TableField("name")
  private String name;

  @TableField("sku_code")
  private String skuCode;

  /** dict_item.code，presaleMubitikeType */
  @TableField("ticket_type")
  private Integer ticketType;

  /** dict_item.code，presaleDeliveryType：1=虚拟 2=实体 */
  @TableField("delivery_type")
  private Integer deliveryType;

  @TableField("stock")
  private Integer stock;

  @TableField("points")
  private Integer points;

  @TableField("ship_days")
  private Integer shipDays;

  /** 规格图集（多张），每规格独立 */
  @TableField(value = "images", typeHandler = StringArrayTypeHandler.class)
  private List<String> images;

  /** 规格级特典名称 */
  @TableField("bonus_title")
  private String bonusTitle;

  /** 规格级特典图片URL数组 */
  @TableField(value = "bonus_images", typeHandler = StringArrayTypeHandler.class)
  private List<String> bonusImages;

  /** 规格级特典说明 */
  @TableField("bonus_description")
  private String bonusDescription;

  /** 规格级特典数量 */
  @TableField("bonus_quantity")
  private Integer bonusQuantity;

  /** 多档价格 [{label,price}]，与 price 并存时以 priceItems 为准 */
  @TableField(value = "price_items", typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> priceItems;

  /** 是否含购票特典：true=特典あり，false=特典なし */
  @TableField("bonus_included")
  private Boolean bonusIncluded;

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
