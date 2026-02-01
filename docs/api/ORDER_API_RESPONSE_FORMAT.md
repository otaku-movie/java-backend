# 订单相关接口返回格式文档

## 统一响应格式

所有接口都使用 `RestBean<T>` 作为统一响应格式：

```json
{
  "code": 200,           // 状态码：200=成功，其他=错误码
  "data": {},            // 响应数据（成功时）或额外信息（错误时）
  "message": "操作成功"  // 响应消息（支持国际化）
}
```

---

## 1. 创建订单

### 1.1 成功响应

**接口**: `POST /api/movieOrder/create`

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

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "id": null,                    // 订单ID（异步创建，可能为null）
    "movieShowTimeId": 34701,
    "orderNumber": "MTa1b2c3d4e5f6",  // 订单号（全局唯一）
    "orderTotal": 2500.00,         // 订单总价
    "orderState": null,            // 订单状态（异步创建中）
    "payDeadline": "2026-01-30 22:25:55"  // 支付截止时间（订单创建时间 + 15分钟）
  },
  "message": "保存成功"
}
```

### 1.2 失败响应 - 部分座位不可用

**失败响应**:
```json
{
  "code": 3203,  // SEAT_OCCUPIED
  "data": {
    "unavailableSeatIds": [28084, 28085],      // 不可用座位ID列表
    "unavailableSeatNames": ["A1", "A2"],     // 不可用座位名称列表
    "unavailableCount": 2,                     // 不可用座位数量
    "totalCount": 3                            // 总座位数量
  },
  "message": "座位已被其他用户选择或锁定，请重新选择，座位号是A1, A2"
}
```

### 1.3 其他失败响应

**座位未选中**:
```json
{
  "code": 3205,  // SEAT_NOT_SELECTED
  "data": null,
  "message": "当前没有有效的选座记录，请重新选座"
}
```

**座位冲突**:
```json
{
  "code": 3201,  // SEAT_CONFLICT
  "data": null,
  "message": "座位冲突，冲突的座位为：A1"
}
```

---

## 2. 支付订单

### 2.1 成功响应

**接口**: `POST /api/movieOrder/pay`

**请求体**:
```json
{
  "orderNumber": "MTa1b2c3d4e5f6",
  "payId": 1
}
```

**成功响应**（异步处理，立即返回）:
```json
{
  "code": 200,
  "data": null,
  "message": "保存成功"
}
```

**注意**: 支付是异步处理的（10秒后完成），实际支付结果需要通过订单详情接口查询。

### 2.2 失败响应

**订单不存在**:
```json
{
  "code": 5101,  // ORDER_NOT_FOUND
  "data": null,
  "message": "订单不存在"
}
```

**订单状态不允许支付**:
```json
{
  "code": 0,  // ERROR
  "data": null,
  "message": "订单状态不允许支付"
}
```

**支付失败**（部分座位不可用，全部失败）:
- 订单状态更新为失败
- 所有座位从 Redis 删除
- 返回错误消息

---

## 3. 订单详情

### 3.1 成功响应

**接口**: `GET /api/movieOrder/detail?id=123`

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "orderNumber": "MTa1b2c3d4e5f6",
    "orderTotal": 2500.00,
    "orderState": 1,              // 1=已创建，2=成功，3=失败，4=取消，5=超时
    "payMethod": "信用卡",
    "payNumber": null,
    "payState": 1,                // 1=待支付，2=支付中，3=支付成功，4=支付失败
    "payTotal": null,
    "orderTime": "2026-01-30 22:10:55",
    "payTime": null,
    "payDeadline": "2026-01-30 22:25:55",  // 支付截止时间（如果订单未支付）
    "date": "2026-11-26",
    "startTime": "12:00",
    "endTime": "15:00",
    "movieId": 34700,
    "movieName": "劇場版『ゾンビランドサガ』",
    "originalName": "Zombie Land Saga",
    "moviePoster": "https://example.com/poster.jpg",
    "cinemaId": 1,
    "cinemaName": "TOHOシネマズ",
    "theaterHallName": "1号厅",
    "specName": "IMAX",
    "cinemaFullAddress": "東京都...",
    "movieShowTimeId": 34701,
    "theaterHallId": 34,
    "seat": [
      {
        "movieOrderId": 123,
        "seatId": 28084,
        "seatName": "A1",
        "x": 1,
        "y": 5,
        "movieTicketTypeId": 1,
        "movieTicketTypeName": "一般",
        "movieTicketTypePrice": 2000.00,
        "areaPrice": 0.00,
        "plusPrice": 500.00
      }
    ]
  },
  "message": "获取成功"
}
```

### 3.2 失败响应

**订单不存在**:
```json
{
  "code": 0,  // ERROR
  "data": null,
  "message": "用户不存在"  // 注意：这里应该返回订单不存在的消息
}
```

---

## 4. 订单列表

### 4.1 成功响应

**接口**: `POST /api/admin/movieOrder/list`

**请求体**:
```json
{
  "page": 1,
  "pageSize": 10,
  "orderNumber": null,
  "orderState": null,
  "payState": null,
  "movieName": null,
  "cinemaName": null
}
```

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "page": 1,
    "total": 100,
    "pageSize": 10,
    "list": [
      {
        "id": 123,
        "orderNumber": "MTa1b2c3d4e5f6",
        "orderTotal": 2500.00,
        "orderState": 1,
        "payMethod": "信用卡",
        "payNumber": null,
        "payState": 1,
        "payTotal": null,
        "orderTime": "2026-01-30 22:10:55",
        "payTime": null,
        "payDeadline": "2026-01-30 22:25:55",  // 如果订单未支付
        "date": "2026-11-26",
        "startTime": "12:00",
        "endTime": "15:00",
        "movieId": 34700,
        "movieName": "劇場版『ゾンビランドサガ』",
        "originalName": "Zombie Land Saga",
        "moviePoster": "https://example.com/poster.jpg",
        "cinemaId": 1,
        "cinemaName": "TOHOシネマズ",
        "theaterHallName": "1号厅",
        "specName": "IMAX",
        "cinemaFullAddress": "東京都...",
        "movieShowTimeId": 34701,
        "theaterHallId": 34,
        "seat": [
          {
            "movieOrderId": 123,
            "seatId": 28084,
            "seatName": "A1",
            "x": 1,
            "y": 5,
            "movieTicketTypeId": 1,
            "movieTicketTypeName": "一般",
            "movieTicketTypePrice": 2000.00,
            "areaPrice": 0.00,
            "plusPrice": 500.00
          }
        ]
      }
    ]
  },
  "message": "获取成功"
}
```

---

## 5. 我的票据

### 5.1 成功响应

**接口**: `POST /api/movieOrder/myTickets`

**请求体**:
```json
{
  "page": 1,
  "pageSize": 10,
  "movieName": null,
  "cinemaName": null
}
```

**成功响应**:
```json
{
  "code": 200,
  "data": {
    "page": 1,
    "total": 50,
    "pageSize": 10,
    "list": [
      {
        "id": 123,
        "orderTotal": 2500.00,
        "orderState": 2,              // 2=成功
        "payMethod": "信用卡",
        "payNumber": null,
        "payState": 3,                 // 3=支付成功
        "payTotal": 2500.00,
        "orderTime": "2026-01-30 22:10:55",
        "payTime": "2026-01-30 22:11:05",
        "payDeadline": null,           // 已支付，无截止时间
        "date": "2026-11-26",
        "startTime": "12:00",
        "endTime": "15:00",
        "movieId": 34700,
        "movieName": "劇場版『ゾンビランドサガ』",
        "originalName": "Zombie Land Saga",
        "moviePoster": "https://example.com/poster.jpg",
        "cinemaId": 1,
        "cinemaName": "TOHOシネマズ",
        "cinemaFullAddress": "東京都...",
        "cinemaTel": "03-1234-5678",
        "theaterHallId": 34,
        "theaterHallName": "1号厅",
        "specName": "IMAX",
        "movieShowTimeId": 34701,
        "seat": [
          {
            "movieOrderId": 123,
            "seatId": 28084,
            "seatName": "A1",
            "x": 1,
            "y": 5,
            "movieTicketTypeId": 1,
            "movieTicketTypeName": "一般",
            "movieTicketTypePrice": 2000.00,
            "areaPrice": 0.00,
            "plusPrice": 500.00
          }
        ]
      }
    ]
  },
  "message": "获取成功"
}
```

---

## 6. 取消订单

### 6.1 成功响应

**接口**: `POST /api/movieOrder/cancel`

**请求体**:
```json
{
  "orderNumber": "MTa1b2c3d4e5f6"
}
```

**成功响应**:
```json
{
  "code": 200,
  "data": null,
  "message": "保存成功"
}
```

### 6.2 失败响应

**订单不存在**:
```json
{
  "code": 5101,  // ORDER_NOT_FOUND
  "data": null,
  "message": "订单不存在"
}
```

**订单状态不允许取消**:
```json
{
  "code": 3105,  // ORDER_STATE_INVALID
  "data": null,
  "message": "订单状态不允许此操作"
}
```

---

## 7. 订单超时

### 7.1 成功响应

**接口**: `POST /api/movieOrder/timeout`

**请求体**:
```json
{
  "orderNumber": "MTa1b2c3d4e5f6"
}
```

**成功响应**:
```json
{
  "code": 200,
  "data": null,
  "message": "保存成功"
}
```

---

## 状态码说明

### 成功状态码
- `200`: 操作成功

### 错误状态码

#### 订单相关 (3100-3199)
- `3101`: 订单正在创建中
- `3102`: 订单已支付，无法重复支付
- `3103`: 订单已取消，无法操作
- `3104`: 订单已超时
- `3105`: 订单状态不允许此操作
- `3106`: 订单创建失败

#### 选座相关 (3200-3299)
- `3201`: 选座冲突（座位已被其他用户选择）
- `3202`: 选座请求正在处理中
- `3203`: 座位已被其他用户选择或锁定
- `3204`: 选座保存失败
- `3205`: 当前没有有效的选座记录

#### 资源相关 (5100-5199)
- `5101`: 订单不存在

---

## 订单状态说明

- `1`: 已创建（待支付）
- `2`: 成功（已支付）
- `3`: 失败
- `4`: 取消
- `5`: 超时

## 支付状态说明

- `1`: 待支付
- `2`: 支付中
- `3`: 支付成功
- `4`: 支付失败

---

## 注意事项

1. **创建订单**: 订单是异步创建的，返回的订单对象可能没有 `id`，需要通过 `orderNumber` 查询订单详情
2. **支付订单**: 支付是异步处理的（10秒后完成），需要轮询订单详情接口获取支付结果
3. **支付截止时间**: 只有订单状态为"已创建"且未支付时，才会返回 `payDeadline` 字段
4. **座位状态**: 从 Redis 获取座位状态，`locked` 状态表示已创建订单但未支付，`sold` 状态表示已支付
5. **部分座位不可用**: 
   - 创建订单时：返回不可用座位的详细信息（ID、名称、数量）
   - 支付时：如果部分座位不可用，支付失败（全部成功或全部失败）
