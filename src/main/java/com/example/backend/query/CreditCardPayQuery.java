package com.example.backend.query;

import lombok.Data;

import java.util.Map;

@Data
public class CreditCardPayQuery {
    private Integer orderId;
    private Integer creditCardId; // 已保存的信用卡ID
    private Map<String, Object> tempCard; // 临时信用卡数据（仅本次使用）
}
