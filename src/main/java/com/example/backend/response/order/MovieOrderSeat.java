package com.example.backend.response.order;

import lombok.Data;

@Data
public class MovieOrderSeat {
  Long movieOrderId;
  Integer seatX;
  Integer seatY;
  String seatName;
  String movieTicketTypeName;
  String areaName;
  Integer areaPrice;
}
