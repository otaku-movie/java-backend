package com.example.backend.query.order;

import lombok.Data;

@Data
public class MovieOrderListQuery {
  private Integer page;
  private Integer pageSize;
  /** 业务订单号（movie_order.order_number），精确匹配 */
  private String orderNumber;
  private Integer movieId;
  private Integer cinemaId;
  private Integer theaterHallId;
  private Integer orderState;
  private Integer payState;
  private String orderStartTime;
  private String orderEndTime;

  /**
   * 排序字段（白名单）：orderTime 订单创建时间、orderTotal 订单总价、payTotal 支付金额、
   * payTime 支付时间、startTime 上映开始时间。
   */
  private String sortField;
  /** 排序方向：asc 升序、desc 降序 */
  private String sortOrder;

  public MovieOrderListQuery() {
    this.page = 1; // 默认页数为1
    this.pageSize = 10; // 默认页面大小为10
    this.sortField = "orderTime";
    this.sortOrder = "desc";
  }
}