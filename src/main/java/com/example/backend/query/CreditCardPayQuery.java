package com.example.backend.query;

import java.util.Map;

public class CreditCardPayQuery {
    private Long orderId;
    private Integer creditCardId; // 已保存的信用卡ID
    private Map<String, Object> tempCard; // 临时信用卡数据（仅本次使用）

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getCreditCardId() { return creditCardId; }
    public void setCreditCardId(Integer creditCardId) { this.creditCardId = creditCardId; }
    public Map<String, Object> getTempCard() { return tempCard; }
    public void setTempCard(Map<String, Object> tempCard) { this.tempCard = tempCard; }
}
