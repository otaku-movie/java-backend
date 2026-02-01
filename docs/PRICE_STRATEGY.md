# 电影票价策略文档

## 一、概述

电影票价格采用**叠加式计算**，由以下三部分组成：

1. **票种基础价**：按年龄段（成人、儿童等）的基础票价
2. **放映类型加价**：3D 放映的加价（2D 无加价）
3. **规格加价**：IMAX、Dolby 等增强规格的加价

## 二、计算公式

```
最终票价 = 票种基础价 + 3D加价(若有) + 规格加价(若有)
```

### 2.1 示例

| 场次类型 | 票种(成人 ¥1000) | 3D加价 | IMAX加价 | 最终票价 |
|----------|------------------|--------|----------|----------|
| 2D 普通 | ¥1000 | - | - | ¥1000 |
| 3D 普通 | ¥1000 | +¥300 | - | ¥1300 |
| IMAX 2D | ¥1000 | - | +¥500 | ¥1500 |
| IMAX 3D | ¥1000 | +¥300 | +¥500 | ¥1800 |

### 2.2 特殊情况

- **2D**：无 3D 加价，无规格加价时，仅使用票种价
- **无规格场次**：`movie_show_time.spec_id` 为空时，不叠加规格加价
- **座位区域价**：部分座位有 `area_price`，在最终票价基础上额外叠加（由选座逻辑处理）

## 三、数据模型

### 3.1 核心表

| 表名 | 作用 |
|------|------|
| `movie_ticket_type` | 票种（成人/儿童等），`price` 为 2D 基础价 |
| `dict` + `dict_item` | 放映类型字典，code=dimensionType：2D(code=1)、3D(code=2) |
| `cinema_price_config` | 3D 加价配置：`cinema_id` + `dimension_type` → `surcharge` |
| `cinema_spec_spec` | 规格加价：`cinema_id` + `spec_id` → `plus_price` |
| `movie_show_time` | 场次，关联 `dimension_type`、`spec_id` |

### 3.2 放映类型（dimensionType）

- 使用字典 `dict.code = 'dimensionType'`
- `dict_item.code = 1` → 2D
- `dict_item.code = 2` → 3D

### 3.3 票价配置关系

```
cinema (影院)
  ├── movie_ticket_type (票种，按影院或全局)
  ├── cinema_price_config (3D 加价，按 dimension_type)
  └── cinema_spec_spec (规格加价，按 spec_id)

movie_show_time (场次)
  ├── dimension_type → dict_item (2D/3D)
  └── spec_id → movie_spec (IMAX/Dolby 等)
```

## 四、配置说明

### 4.1 票种基础价（movie_ticket_type）

- `price` 表示该票种的 2D 基础价
- 不同票种（成人、学生、儿童等）可设置不同价格

### 4.2 3D 加价（cinema_price_config）

- 仅需为 3D 配置，2D 加价为 0，无需配置
- 字段：`cinema_id`、`dimension_type`（3D 的 dict_item.id）、`surcharge`

```sql
-- 示例：影院 1 的 3D 加价为 300
INSERT INTO cinema_price_config (cinema_id, dimension_type, surcharge)
SELECT 1, di.id, 300
FROM dict_item di
JOIN dict d ON di.dict_id = d.id
WHERE d.code = 'dimensionType' AND di.code = 2;
```

### 4.3 规格加价（cinema_spec_spec）

- 关联 `cinema_id` 与 `spec_id`（IMAX、Dolby 等）
- 字段：`plus_price` 为整数加价金额

## 五、迁移与初始化

### 5.1 执行顺序

1. `init_display_type_dict.sql`：初始化放映类型字典（2D、3D）
2. `display_type_and_price_rules.sql`：创建 `cinema_price_config`，为 `movie_show_time` 增加 `dimension_type`
3. 按需配置 `cinema_price_config` 和 `cinema_spec_spec`

### 5.2 存量数据

- 已有 `movie_show_time` 默认设为 2D（`dimension_type` 取 2D 的 dict_item.id）
- 3D 场次需手动更新 `dimension_type` 为 3D 对应项

## 六、代码入口

| 组件 | 说明 |
|------|------|
| `TicketPriceService.calculatePrice()` | 票价计算入口 |
| `MovieOrderService` | 下单时调用 `TicketPriceService` 计算每个座位价格 |
| `CinemaPriceConfigMapper.getSurcharge()` | 查询 3D 加价 |

---

## 七、接口说明

### 7.1 创建订单（含票价计算）

**接口**: `POST /api/movieOrder/create`  
**说明**: 下单时根据场次、票种、座位自动计算票价（调用 `TicketPriceService`）。

**请求体**:
```json
{
  "movieShowTimeId": 34701,
  "seat": [
    {
      "x": 1,
      "y": 5,
      "seatId": 28084,
      "movieTicketTypeId": 1
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| movieShowTimeId | Integer | 是 | 场次 ID |
| seat | Array | 是 | 座位列表 |
| seat[].x | Integer | 是 | 座位 x 坐标 |
| seat[].y | Integer | 是 | 座位 y 坐标 |
| seat[].seatId | Integer | 是 | 座位 ID |
| seat[].movieTicketTypeId | Integer | 是 | 票种 ID（成人/儿童等），用于取基础价 |

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "orderNumber": "MTa1b2c3d4e5f6",
    "orderTotal": 2500.00,
    "payDeadline": "2026-01-30 22:25:55"
  }
}
```

- `orderTotal`：按「票种 + 3D 加价 + 规格加价 + 区域价」汇总得出。

---

### 7.2 用户选座（展示票价相关信息）

**接口**: `GET /api/movie_show_time/user_select_seat?movieShowTimeId={id}`  
**说明**: 返回选座页所需数据，含放映类型、规格等，便于前端展示。

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "movieShowTimeId": 34701,
    "cinemaId": 1,
    "theaterHallId": 34,
    "displayTypeId": 2,
    "specId": 1,
    "specName": "IMAX",
    "plusPrice": 500,
    "seat": [
      {
        "seatId": 28084,
        "seatName": "A1",
        "x": 1,
        "y": 5,
        "movieTicketTypeId": 1,
        "areaPrice": 0,
        "areaName": "普通区"
      }
    ]
  }
}
```

| 字段 | 说明 |
|------|------|
| displayTypeId | 放映类型 dict_item.id（1=2D, 2=3D） |
| specId | 规格 ID（IMAX、Dolby 等） |
| plusPrice | 规格加价金额（仅供参考，实际价格在创建订单时计算） |
| seat[].movieTicketTypeId | 票种 ID |
| seat[].areaPrice | 座位区域加价 |

---

### 7.3 订单详情 / 订单列表 / 我的票据（票价明细）

**接口**:
- 订单详情：`GET /api/movieOrder/detail?id={id}`
- 订单列表：`POST /api/admin/movieOrder/list`
- 我的票据：`POST /api/movieOrder/myTickets`

**座位价格字段**（`seat` 中每个座位的明细）:

| 字段 | 类型 | 说明 |
|------|------|------|
| movieTicketTypePrice | BigDecimal | 该座位最终票价（已含票种 + 3D + 规格） |
| areaPrice | BigDecimal | 区域加价 |
| plusPrice | BigDecimal | 规格加价（展示用，已包含在 movieTicketTypePrice 中） |
| movieTicketTypeName | String | 票种名称 |

单座总价 = `movieTicketTypePrice` + `areaPrice`。

---

### 7.4 票种管理（基础价配置）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/cinema/ticketType/list` | POST | 按影院查票种列表（只读） |

票种的增删改已合并到**影院保存接口** `POST /api/admin/cinema/save`，请求体中包含 `ticketType` 数组（`name`、`price`），保存时先删除该影院原有票种再按列表插入。

- `price`：2D 基础价（单位：元）。

---

### 7.5 影院规格（规格加价）

**接口**: `GET /api/cinema/spec?cinemaId={id}`  
**说明**: 返回该影院支持的规格及 `plus_price`（规格加价）。

---

### 7.6 票价配置（管理端）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/cinema/priceConfig/list` | POST | 按影院查询放映类型加价列表 |
| `/api/admin/cinema/priceConfig/save` | POST | 新增/修改 3D 加价 |
| `/api/admin/cinema/priceConfig/remove` | DELETE | 删除加价配置 |

**管理端入口**: 影院列表 → 票种 → 切换至「放映类型加价」标签页。
