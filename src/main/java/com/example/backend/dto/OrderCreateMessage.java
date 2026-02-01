package com.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建消息（通过 RabbitMQ 异步创建订单）
 */
@Data
public class OrderCreateMessage {
  /**
   * 用户ID
   */
  private Integer userId;

  /**
   * 场次ID
   */
  private Integer movieShowTimeId;

  /**
   * 影厅ID
   */
  private Integer theaterHallId;

  /**
   * 影院ID
   */
  private Integer cinemaId;

  /**
   * 订单号（业务订单号，在发送消息前已生成）
   */
  private String orderNumber;

  /**
   * 订单总价
   */
  private BigDecimal orderTotal;

  /**
   * 座位信息列表
   */
  private List<SeatInfo> seats;

  /**
   * 座位信息
   */
  @Data
  public static class SeatInfo {
    /**
     * 座位ID
     */
    private Integer seatId;

    /**
     * 座位名称
     */
    private String seatName;

    /**
     * X坐标
     */
    private Integer x;

    /**
     * Y坐标
     */
    private Integer y;

    /**
     * 票种ID
     */
    private Integer movieTicketTypeId;

    /**
     * 区域价格
     */
    private BigDecimal areaPrice;

    /**
     * 票种价格
     */
    private BigDecimal movieTicketTypePrice;

    /**
     * 加价
     */
    private BigDecimal plusPrice;
  }
}
