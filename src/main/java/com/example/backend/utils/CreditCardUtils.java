package com.example.backend.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class CreditCardUtils {

    // 卡号验证正则表达式
    private static final Pattern VISA_PATTERN = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern MASTERCARD_PATTERN = Pattern.compile("^5[1-5][0-9]{14}$|^2(?:2(?:2[1-9]|[3-9][0-9])|[3-6][0-9][0-9]|7(?:[01][0-9]|20))[0-9]{12}$");
    private static final Pattern JCB_PATTERN = Pattern.compile("^(?:2131|1800|35\\d{3})\\d{11}$");
    private static final Pattern UNIONPAY_PATTERN = Pattern.compile("^(62|81)[0-9]{14,17}$");

    /**
     * 根据卡号识别卡类型
     */
    public static String detectCardType(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "Unknown";
        }
        
        // 移除空格和连字符
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (VISA_PATTERN.matcher(cleanNumber).matches()) {
            return "Visa";
        } else if (MASTERCARD_PATTERN.matcher(cleanNumber).matches()) {
            return "MasterCard";
        } else if (JCB_PATTERN.matcher(cleanNumber).matches()) {
            return "JCB";
        } else if (UNIONPAY_PATTERN.matcher(cleanNumber).matches()) {
            return "UnionPay";
        } else {
            return "Unknown";
        }
    }

    /**
     * 验证信用卡号是否有效（使用Luhn算法）
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return false;
        }
        
        // 移除空格和连字符
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // 检查是否全为数字
        if (!cleanNumber.matches("\\d+")) {
            return false;
        }
        
        // 检查长度
        int length = cleanNumber.length();
        if (length < 13 || length > 19) {
            return false;
        }
        
        // 使用Luhn算法验证
        return isValidLuhn(cleanNumber);
    }

    /**
     * Luhn算法验证
     */
    private static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    /**
     * 验证有效期格式是否正确（MM/YY）
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        if (!StringUtils.hasText(expiryDate)) {
            return false;
        }
        
        return expiryDate.matches("^(0[1-9]|1[0-2])\\/\\d{2}$");
    }

    /**
     * 验证CVV格式
     */
    public static boolean isValidCvv(String cvv, String cardType) {
        if (!StringUtils.hasText(cvv)) {
            return false;
        }
        
        // American Express 使用4位CVV，其他卡使用3位
        if ("AmericanExpress".equals(cardType)) {
            return cvv.matches("\\d{4}");
        } else {
            return cvv.matches("\\d{3}");
        }
    }

    /**
     * 获取卡号的后四位
     */
    public static String getLastFourDigits(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "";
        }
        
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        if (cleanNumber.length() >= 4) {
            return cleanNumber.substring(cleanNumber.length() - 4);
        }
        
        return cleanNumber;
    }

    /**
     * 格式化卡号显示（隐藏中间部分）
     */
    public static String maskCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "";
        }
        
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        if (cleanNumber.length() >= 4) {
            String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        
        return cleanNumber;
    }
}
