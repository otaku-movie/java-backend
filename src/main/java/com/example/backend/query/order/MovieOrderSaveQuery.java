package com.example.backend.query.order;

import com.example.backend.query.SeatGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MovieOrderSaveQuery {
  // 场次 id
  @NotNull
  Integer movieShowTimeId;
  @NotNull
  List<SeatGroup> seat;
  /** ムビチケ前売り券：购票号码（10位），与 mubitikePassword、mubitikeUseCount 一起传入时生效 */
  String mubitikeCode;
  /** ムビチケ前売り券：密码（4位） */
  String mubitikePassword;
  /** 使用前売り券的张数（前 N 个座位基础价抵消，3D/IMAX 等加价照常收取） */
  Integer mubitikeUseCount;
}
