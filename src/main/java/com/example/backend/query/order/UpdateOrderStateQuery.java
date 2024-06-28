package com.example.backend.query.order;

import lombok.Data;

@Data
public class UpdateOrderStateQuery {
  Integer id;
  Integer payState;
  Integer orderState;
}
