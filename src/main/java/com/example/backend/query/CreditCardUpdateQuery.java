package com.example.backend.query;

import jakarta.validation.constraints.NotNull;

public class CreditCardUpdateQuery {
    
    @NotNull(message = "信用卡ID不能为空")
    private Integer id;
    
    private String cardHolderName;
    
    private String expiryDate;
    
    private Boolean isDefault;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
