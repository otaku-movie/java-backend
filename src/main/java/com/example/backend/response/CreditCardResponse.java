package com.example.backend.response;

import lombok.Data;

import java.util.Date;

@Data
public class CreditCardResponse {
    private Integer id;
    private String cardType;
    private String lastFourDigits;
    private String cardHolderName;
    private String expiryDate;
    private Boolean isDefault;
    private Date createTime;
    
    // 不返回完整卡号、CVV等敏感信息
}
