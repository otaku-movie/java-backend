package com.example.backend;

import com.example.backend.utils.CreditCardUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 信用卡功能测试用例
 */
@SpringBootTest
public class CreditCardApiTest {

    @Test
    public void testCreditCardValidation() {
        // 测试有效的Visa卡号
        assertTrue(CreditCardUtils.isValidCardNumber("4242424242424242"));
        assertEquals("Visa", CreditCardUtils.detectCardType("4242424242424242"));
        
        // 测试有效的MasterCard卡号
        assertTrue(CreditCardUtils.isValidCardNumber("5555555555554444"));
        assertEquals("MasterCard", CreditCardUtils.detectCardType("5555555555554444"));
        
        // 测试无效卡号
        assertFalse(CreditCardUtils.isValidCardNumber("1234567890123456"));
        
        // 测试有效期验证
        assertTrue(CreditCardUtils.isValidExpiryDate("12/25"));
        assertFalse(CreditCardUtils.isValidExpiryDate("13/25")); // 无效月份
        assertFalse(CreditCardUtils.isValidExpiryDate("12/2025")); // 错误格式
        
        // 测试CVV验证
        assertTrue(CreditCardUtils.isValidCvv("123", "Visa"));
        assertFalse(CreditCardUtils.isValidCvv("12", "Visa")); // CVV太短
        
        // 测试后四位获取
        assertEquals("4242", CreditCardUtils.getLastFourDigits("4242424242424242"));
        assertEquals("4444", CreditCardUtils.getLastFourDigits("5555 5555 5555 4444"));
        
        // 测试卡号掩码
        assertEquals("**** **** **** 4242", CreditCardUtils.maskCardNumber("4242424242424242"));
    }

    @Test
    public void testCardTypeDetection() {
        // 测试各种卡类型检测
        assertEquals("Visa", CreditCardUtils.detectCardType("4111111111111111"));
        assertEquals("MasterCard", CreditCardUtils.detectCardType("5555555555554444"));
        assertEquals("JCB", CreditCardUtils.detectCardType("3530111333300000"));
        assertEquals("UnionPay", CreditCardUtils.detectCardType("6200000000000005"));
        assertEquals("Unknown", CreditCardUtils.detectCardType("1234567890123456"));
    }
}
