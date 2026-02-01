package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 选座取消消息 DTO
 * 用于 RabbitMQ 延迟消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionCancelMessage implements Serializable {
  private static final long serialVersionUID = 1L;

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
   * 座位坐标列表（x, y）
   */
  private List<SeatCoordinate> seatCoordinates;

  /**
   * 座位坐标内部类
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SeatCoordinate implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer x;
    private Integer y;
    private Integer seatId;
    private String seatName; // 座位名称
  }
}
