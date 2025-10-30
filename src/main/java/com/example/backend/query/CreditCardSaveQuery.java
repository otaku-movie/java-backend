package com.example.backend.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditCardSaveQuery {
    
    @NotBlank(message = "卡号不能为空")
    private String cardNumber;
    
    @NotBlank(message = "持卡人姓名不能为空")
    private String cardHolderName;
    
    @NotBlank(message = "有效期不能为空")
    private String expiryDate;
    
    @NotBlank(message = "CVV不能为空")
    private String cvv;
    
    @NotBlank(message = "卡类型不能为空")
    private String cardType;
    
    @NotNull(message = "是否设为默认卡不能为空")
    private Boolean isDefault;
    
    private Boolean saveCard = true; // 是否保存到数据库，默认为true
}
