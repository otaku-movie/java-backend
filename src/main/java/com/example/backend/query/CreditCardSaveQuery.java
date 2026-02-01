package com.example.backend.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreditCardSaveQuery {
    
    @NotBlank(message = "卡号不能为空")
    private String cardNumber;
    
    @NotBlank(message = "持卡人姓名不能为空")
    private String cardHolderName;
    
    @NotBlank(message = "有效期不能为空")
    private String expiryDate;

    /** 卡类型（可选），当无法从卡号推导时作为 fallback */
    private String cardType;
    
    @NotNull(message = "是否设为默认卡不能为空")
    private Boolean isDefault;
    
    private Boolean saveCard = true; // 是否保存到数据库，默认为true

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public Boolean getSaveCard() { return saveCard; }
    public void setSaveCard(Boolean saveCard) { this.saveCard = saveCard; }
}
