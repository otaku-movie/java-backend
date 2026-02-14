package com.example.backend.query.presale;

import com.example.backend.query.PaginationQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PresaleListQuery extends PaginationQuery {
  /** 按标题模糊 */
  private String title;
  /** 按编码精确 */
  private String code;
  /** 按适用电影ID */
  private Integer movieId;
}
