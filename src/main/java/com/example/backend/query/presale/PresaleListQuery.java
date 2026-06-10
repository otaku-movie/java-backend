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
  /** 按券种筛选（dict_item.code，presaleMubitikeType：1=网络券 2=卡券 3=套票 4=电影票预售券），匹配存在该券种规格的预售 */
  private Integer mubitikeType;
}
