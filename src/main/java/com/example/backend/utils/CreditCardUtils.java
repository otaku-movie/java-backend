package com.example.backend.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class CreditCardUtils {

    // 卡号验证正则表达式
    private static final Pattern VISA_PATTERN = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern MASTERCARD_PATTERN = Pattern.compile("^5[1-5][0-9]{14}$|^2(?:2(?:2[1-9]|[3-9][0-9])|[3-6][0-9][0-9]|7(?:[01][0-9]|20))[0-9]{12}$");
    private static final Pattern AMEX_PATTERN = Pattern.compile("^3[47][0-9]{13}$");
    private static final Pattern JCB_PATTERN = Pattern.compile("^(?:2131|1800|35[0-9]{2})[0-9]{11}$");
    private static final Pattern UNIONPAY_PATTERN = Pattern.compile("^(62|81)[0-9]{14,17}$");
    private static final Pattern DISCOVER_PATTERN = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");
    private static final Pattern DINERS_PATTERN = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]{11}$");

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
        }
        if (MASTERCARD_PATTERN.matcher(cleanNumber).matches()) {
            return "MasterCard";
        }
        if (AMEX_PATTERN.matcher(cleanNumber).matches()) {
            return "AmericanExpress";
        }
        if (JCB_PATTERN.matcher(cleanNumber).matches()) {
            return "JCB";
        }
        if (UNIONPAY_PATTERN.matcher(cleanNumber).matches()) {
            return "UnionPay";
        }
        if (DISCOVER_PATTERN.matcher(cleanNumber).matches()) {
            return "Discover";
        }
        if (DINERS_PATTERN.matcher(cleanNumber).matches()) {
            return "Diners";
        }
        return "Unknown";
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
     * 获取卡号前六位（BIN，用于显示遮罩，符合 PCI DSS 仅显示前6+后4位）
     */
    public static String getFirstSixDigits(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "";
        }
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        return cleanNumber.length() >= 6 ? cleanNumber.substring(0, 6) : cleanNumber;
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
     * 格式化卡号显示（隐藏中间部分，PCI DSS：仅显示前6位+后4位）
     */
    public static String maskCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "";
        }
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        if (cleanNumber.length() >= 10) {
            String firstSix = cleanNumber.substring(0, 6);
            String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
            return firstSix + " ****** " + lastFour;
        }
        if (cleanNumber.length() >= 4) {
            String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return cleanNumber;
    }

    /**
     * 根据前6位和后4位生成遮罩显示（用于已存储的卡，不持有完整卡号时）
     */
    public static String buildMaskedDisplay(String firstSixDigits, String lastFourDigits) {
        if (StringUtils.hasText(firstSixDigits) && StringUtils.hasText(lastFourDigits)) {
            return firstSixDigits + " ****** " + lastFourDigits;
        }
        if (StringUtils.hasText(lastFourDigits)) {
            return "**** **** **** " + lastFourDigits;
        }
        return "";
    }
}
