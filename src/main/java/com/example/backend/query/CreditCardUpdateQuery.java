package com.example.backend.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditCardUpdateQuery {
    
    @NotNull(message = "信用卡ID不能为空")
    private Integer id;
    
    private String cardHolderName;
    
    private String expiryDate;
    
    private Boolean isDefault;
}
