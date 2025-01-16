package com.example.backend.response.order;

import lombok.Data;

@Data
public
class MovieOrderSeat {
  Integer movieOrderId;
  String seatName;
  String movieTicketTypeName;
}
