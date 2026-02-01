package com.example.backend.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 规格ID + 名称 + 加价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecPriceItem {
    private Integer id;
    private String name;
    private BigDecimal plusPrice;
}
