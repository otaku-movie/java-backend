# 信用卡管理 API 文档

## 概述

本文档描述了otaku-movie项目中信用卡管理功能的后端API实现，包括信用卡的增删改查和支付功能。

## API 接口列表

### 1. 获取用户信用卡列表

**接口**: `GET /api/creditCard/list`

**描述**: 获取当前登录用户的所有信用卡

**请求头**:
```
Authorization: Bearer {token}
```

**响应**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": [
        {
            "id": 1,
            "cardType": "Visa",
            "lastFourDigits": "4242",
            "cardHolderName": "TARO YAMADA",
            "expiryDate": "12/25",
            "isDefault": true,
            "createTime": "2024-01-01T10:00:00"
        }
    ]
}
```

### 2. 获取信用卡详情

**接口**: `GET /api/creditCard/detail?id={cardId}`

**描述**: 根据ID获取信用卡详情

**请求参数**:
- `id`: 信用卡ID

**响应**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "id": 1,
        "cardType": "Visa",
        "lastFourDigits": "4242",
        "cardHolderName": "TARO YAMADA",
        "expiryDate": "12/25",
        "isDefault": true,
        "createTime": "2024-01-01T10:00:00"
    }
}
```

### 3. 保存信用卡

**接口**: `POST /api/creditCard/save`

**描述**: 添加新的信用卡

**请求体**:
```json
{
    "cardNumber": "4242 4242 4242 4242",
    "cardHolderName": "TARO YAMADA",
    "expiryDate": "12/25",
    "cvv": "123",
    "cardType": "Visa",
    "isDefault": true,
    "saveCard": true
}
```

**字段说明**:
- `cardNumber`: 信用卡号（必填）
- `cardHolderName`: 持卡人姓名（必填）
- `expiryDate`: 有效期，格式为MM/YY（必填）
- `cvv`: CVV安全码（必填）
- `cardType`: 卡类型，会自动检测（必填）
- `isDefault`: 是否设为默认信用卡（必填）
- `saveCard`: 是否保存到数据库，默认为true

**响应**:
```json
{
    "code": 200,
    "message": "保存成功",
    "data": {
        "id": 1,
        "cardType": "Visa",
        "lastFourDigits": "4242",
        "cardHolderName": "TARO YAMADA",
        "expiryDate": "12/25",
        "isDefault": true,
        "createTime": "2024-01-01T10:00:00"
    }
}
```

### 4. 更新信用卡信息

**接口**: `POST /api/creditCard/update`

**描述**: 更新信用卡信息（仅支持更新持卡人姓名、有效期和默认状态）

**请求体**:
```json
{
    "id": 1,
    "cardHolderName": "NEW NAME",
    "expiryDate": "12/26",
    "isDefault": true
}
```

**响应**:
```json
{
    "code": 200,
    "message": "更新成功"
}
```

### 5. 删除信用卡

**接口**: `DELETE /api/creditCard/delete?id={cardId}`

**描述**: 删除指定的信用卡

**请求参数**:
- `id`: 信用卡ID

**响应**:
```json
{
    "code": 200,
    "message": "删除成功"
}
```

### 6. 设置默认信用卡

**接口**: `POST /api/creditCard/setDefault`

**描述**: 将指定信用卡设为默认

**请求体**:
```json
{
    "id": 1
}
```

**响应**:
```json
{
    "code": 200,
    "message": "设置成功"
}
```

### 7. 信用卡支付

**接口**: `POST /movieOrder/pay`

**描述**: 使用信用卡进行订单支付

**请求体**:

使用已保存的信用卡：
```json
{
    "orderId": 123,
    "creditCardId": 1
}
```

使用临时信用卡（仅本次使用）：
```json
{
    "orderId": 123,
    "tempCard": {
        "cardNumber": "4242 4242 4242 4242",
        "cardHolderName": "TARO YAMADA",
        "expiryDate": "12/25",
        "cvv": "123",
        "cardType": "Visa"
    }
}
```

**响应**:
```json
{
    "code": 200,
    "message": "支付成功"
}
```

## 数据库表结构

```sql
CREATE TABLE IF NOT EXISTS credit_cards (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    card_number VARCHAR(255) NOT NULL COMMENT '信用卡号(加密存储)',
    card_holder_name VARCHAR(100) NOT NULL COMMENT '持卡人姓名',
    expiry_date VARCHAR(10) NOT NULL COMMENT '有效期(MM/YY)',
    cvv VARCHAR(255) NOT NULL COMMENT 'CVV安全码(加密存储)',
    card_type VARCHAR(50) NOT NULL COMMENT '卡类型(Visa, MasterCard, JCB, UnionPay)',
    last_four_digits VARCHAR(4) NOT NULL COMMENT '卡号后四位',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认信用卡',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志(0-未删除, 1-已删除)',
    
    INDEX idx_user_id (user_id),
    INDEX idx_deleted (deleted),
    INDEX idx_is_default (is_default),
    
    CONSTRAINT fk_credit_cards_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT='信用卡表';
```

## 支持的信用卡类型

- **Visa**: 以4开头，13-19位数字
- **MasterCard**: 以5开头，16位数字
- **JCB**: 以35开头，16位数字
- **UnionPay**: 以62或81开头，14-19位数字

## 安全特性

1. **数据加密**: 信用卡号和CVV会进行加密存储
2. **权限验证**: 用户只能访问自己的信用卡
3. **数据脱敏**: API响应中不包含完整卡号和CVV
4. **Luhn算法验证**: 对信用卡号进行有效性验证
5. **格式验证**: 对有效期、CVV等进行格式验证

## 业务逻辑

1. **默认信用卡**: 每个用户只能有一张默认信用卡
2. **第一张卡**: 用户添加的第一张信用卡自动设为默认
3. **删除默认卡**: 删除默认信用卡时，会自动将另一张卡设为默认
4. **临时支付**: 支持不保存信用卡信息，仅用于本次支付

## 错误码说明

- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未登录或token无效
- `403`: 权限不足
- `500`: 服务器内部错误

## 集成指南

### 前端集成示例（与otaku_movie Flutter应用对应）

1. **获取信用卡列表**对应Flutter中的`_loadCreditCards()`方法
2. **添加信用卡**对应Flutter中的`AddCreditCard`页面功能
3. **信用卡支付**对应Flutter中的`SelectCreditCard`页面的支付功能

### 与现有订单系统集成

信用卡支付功能已集成到现有的`MovieOrderController`中，支持：
- 使用已保存的信用卡支付
- 使用临时信用卡支付（不保存到数据库）
- 自动订单状态更新
