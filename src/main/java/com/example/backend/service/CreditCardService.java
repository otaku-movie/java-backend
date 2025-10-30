package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.CreditCard;
import com.example.backend.mapper.CreditCardMapper;
import com.example.backend.query.CreditCardSaveQuery;
import com.example.backend.query.CreditCardUpdateQuery;
import com.example.backend.response.CreditCardResponse;
import com.example.backend.utils.CreditCardUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditCardService extends ServiceImpl<CreditCardMapper, CreditCard> {

    @Autowired
    private CreditCardMapper creditCardMapper;

    /**
     * 获取当前用户的信用卡列表
     */
    public List<CreditCardResponse> getUserCreditCards() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<CreditCard> creditCards = creditCardMapper.selectByUserId(userId);
        
        return creditCards.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户的信用卡
     */
    public CreditCard getUserCreditCard(Integer cardId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        return creditCardMapper.selectByIdAndUserId(cardId, userId);
    }

    /**
     * 保存信用卡
     */
    @Transactional
    public CreditCardResponse saveCreditCard(CreditCardSaveQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        // 验证信用卡信息
        validateCreditCardData(query);
        
        CreditCard creditCard = new CreditCard();
        BeanUtils.copyProperties(query, creditCard);
        creditCard.setUserId(userId);
        
        // 自动检测卡类型
        String detectedCardType = CreditCardUtils.detectCardType(query.getCardNumber());
        creditCard.setCardType(detectedCardType);
        
        // 设置后四位数字
        creditCard.setLastFourDigits(CreditCardUtils.getLastFourDigits(query.getCardNumber()));
        
        // 如果设为默认卡，先取消其他默认卡
        if (Boolean.TRUE.equals(query.getIsDefault())) {
            clearUserDefaultCards(userId);
        }
        
        // 如果是用户的第一张卡，自动设为默认
        List<CreditCard> existingCards = creditCardMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(existingCards)) {
            creditCard.setIsDefault(true);
        }
        
        creditCardMapper.insert(creditCard);
        return convertToResponse(creditCard);
    }

    /**
     * 更新信用卡信息
     */
    @Transactional
    public void updateCreditCard(CreditCardUpdateQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        CreditCard existingCard = creditCardMapper.selectByIdAndUserId(query.getId(), userId);
        if (existingCard == null) {
            throw new RuntimeException("信用卡不存在或无权限访问");
        }
        
        CreditCard updateCard = new CreditCard();
        BeanUtils.copyProperties(query, updateCard);
        
        // 如果设为默认卡，先取消其他默认卡
        if (Boolean.TRUE.equals(query.getIsDefault())) {
            clearUserDefaultCards(userId);
        }
        
        creditCardMapper.updateById(updateCard);
    }

    /**
     * 删除信用卡
     */
    @Transactional
    public void deleteCreditCard(Integer cardId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        CreditCard existingCard = creditCardMapper.selectByIdAndUserId(cardId, userId);
        if (existingCard == null) {
            throw new RuntimeException("信用卡不存在或无权限访问");
        }
        
        // 如果删除的是默认卡，需要设置另一张卡为默认卡
        if (Boolean.TRUE.equals(existingCard.getIsDefault())) {
            List<CreditCard> userCards = creditCardMapper.selectByUserId(userId);
            for (CreditCard card : userCards) {
                if (!card.getId().equals(cardId)) {
                    card.setIsDefault(true);
                    creditCardMapper.updateById(card);
                    break;
                }
            }
        }
        
        creditCardMapper.deleteById(cardId);
    }

    /**
     * 设置默认信用卡
     */
    @Transactional
    public void setDefaultCard(Integer cardId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        CreditCard existingCard = creditCardMapper.selectByIdAndUserId(cardId, userId);
        if (existingCard == null) {
            throw new RuntimeException("信用卡不存在或无权限访问");
        }
        
        // 取消其他默认卡
        clearUserDefaultCards(userId);
        
        // 设置为默认卡
        existingCard.setIsDefault(true);
        creditCardMapper.updateById(existingCard);
    }

    /**
     * 取消用户的所有默认信用卡设置
     */
    private void clearUserDefaultCards(Integer userId) {
        QueryWrapper<CreditCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_default", true);
        
        List<CreditCard> defaultCards = creditCardMapper.selectList(queryWrapper);
        for (CreditCard card : defaultCards) {
            card.setIsDefault(false);
            creditCardMapper.updateById(card);
        }
    }

    /**
     * 验证信用卡是否属于当前用户
     */
    public boolean validateCreditCardOwnership(Integer cardId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        CreditCard card = creditCardMapper.selectByIdAndUserId(cardId, userId);
        return card != null;
    }

    /**
     * 验证信用卡数据
     */
    private void validateCreditCardData(CreditCardSaveQuery query) {
        // 验证卡号
        if (!CreditCardUtils.isValidCardNumber(query.getCardNumber())) {
            throw new RuntimeException("信用卡号格式无效");
        }
        
        // 验证有效期
        if (!CreditCardUtils.isValidExpiryDate(query.getExpiryDate())) {
            throw new RuntimeException("有效期格式无效，应为MM/YY格式");
        }
        
        // 验证CVV
        String detectedCardType = CreditCardUtils.detectCardType(query.getCardNumber());
        if (!CreditCardUtils.isValidCvv(query.getCvv(), detectedCardType)) {
            throw new RuntimeException("CVV格式无效");
        }
    }

    /**
     * 转换为响应对象
     */
    private CreditCardResponse convertToResponse(CreditCard creditCard) {
        CreditCardResponse response = new CreditCardResponse();
        BeanUtils.copyProperties(creditCard, response);
        return response;
    }
}
