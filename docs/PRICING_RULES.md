# 票价叠加规则

## 计算公式

```
票价 = 票种基础价(movie_ticket_type) + 3D加价(若有) + 规格加价(cinema_spec_spec.plus_price)
```

| 场次类型 | 公式 |
|---------|------|
| 2D | 票种价 |
| 3D | 票种价 + 3D加价 |
| IMAX 2D | 票种价 + IMAX加价 |
| IMAX 3D | 票种价 + 3D加价 + IMAX加价 |

## 表结构

- **dict + dict_item** (code=dimensionType): 2D(code=1)、3D(code=2)
- **movie_ticket_type**: 票种（成人/儿童等），price 为 2D 基础价
- **cinema_price_config**: 3D 加价 (cinema_id, dimension_type, surcharge)
- **cinema_spec_spec**: 规格加价 (cinema_id, spec_id, plus_price)
- **movie_show_time.dimension_type**: 放映类型 dict_item.id
- **movie_show_time.spec_id**: 规格(IMAX 等)

## 迁移步骤

1. 执行 `init_display_type_dict.sql`
2. 执行 `display_type_and_price_rules.sql`
3. 为各影院配置 `cinema_price_config`（3D 加价）
