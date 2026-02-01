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
import com.example.backend.exception.BusinessException;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.constants.MessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
     * 根据ID获取用户的信用卡响应（遮罩敏感信息，符合 PCI DSS）
     */
    public CreditCardResponse getCreditCardResponse(Integer cardId) {
        CreditCard card = getUserCreditCard(cardId);
        return card == null ? null : convertToResponse(card);
    }

    /**
     * 保存信用卡
     */
    @Transactional
    public CreditCardResponse saveCreditCard(CreditCardSaveQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        // 验证信用卡信息
        validateCreditCardData(query);

        // PCI DSS: 不存储完整卡号，使用令牌化
        CreditCard creditCard = new CreditCard();
        creditCard.setUserId(userId);
        creditCard.setCardHolderName(query.getCardHolderName());
        creditCard.setExpiryDate(query.getExpiryDate());
        creditCard.setIsDefault(query.getIsDefault());

        // 生成令牌（真实场景应由支付网关如 Stripe 返回 token）
        creditCard.setCardToken(java.util.UUID.randomUUID().toString().replace("-", ""));

        // 仅存储前6位+后4位用于显示遮罩（PCI DSS 允许）
        creditCard.setFirstSixDigits(CreditCardUtils.getFirstSixDigits(query.getCardNumber()));
        creditCard.setLastFourDigits(CreditCardUtils.getLastFourDigits(query.getCardNumber()));

        // 优先从卡号推导卡类型；若推导为 Unknown 且前端有传 cardType 则使用 fallback
        String detectedCardType = CreditCardUtils.detectCardType(query.getCardNumber());
        if ("Unknown".equals(detectedCardType) && StringUtils.hasText(query.getCardType())) {
            creditCard.setCardType(query.getCardType());
        } else {
            creditCard.setCardType(detectedCardType);
        }
        
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
            throw new BusinessException(ResponseCode.CREDIT_CARD_NOT_FOUND, MessageKeys.Error.CREDIT_CARD_NOT_FOUND);
        }

        // 仅更新允许修改的字段，不触碰 token / 卡号相关字段
        if (StringUtils.hasText(query.getCardHolderName())) {
            existingCard.setCardHolderName(query.getCardHolderName());
        }
        if (StringUtils.hasText(query.getExpiryDate())) {
            existingCard.setExpiryDate(query.getExpiryDate());
        }

        if (Boolean.TRUE.equals(query.getIsDefault())) {
            clearUserDefaultCards(userId);
        }
        if (query.getIsDefault() != null) {
            existingCard.setIsDefault(query.getIsDefault());
        }

        creditCardMapper.updateById(existingCard);
    }

    /**
     * 删除信用卡
     */
    @Transactional
    public void deleteCreditCard(Integer cardId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        CreditCard existingCard = creditCardMapper.selectByIdAndUserId(cardId, userId);
        if (existingCard == null) {
            throw new BusinessException(ResponseCode.CREDIT_CARD_NOT_FOUND, MessageKeys.Error.CREDIT_CARD_NOT_FOUND);
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
            throw new BusinessException(ResponseCode.CREDIT_CARD_NOT_FOUND, MessageKeys.Error.CREDIT_CARD_NOT_FOUND);
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
            throw new BusinessException(ResponseCode.PARAMETER_FORMAT_ERROR, MessageKeys.Error.CREDIT_CARD_NUMBER_INVALID);
        }
        
        // 验证有效期
        if (!CreditCardUtils.isValidExpiryDate(query.getExpiryDate())) {
            throw new BusinessException(ResponseCode.PARAMETER_FORMAT_ERROR, MessageKeys.Error.CREDIT_CARD_EXPIRY_INVALID);
        }
    }

    /**
     * 转换为响应对象（不包含敏感数据，卡号仅返回遮罩格式）
     */
    private CreditCardResponse convertToResponse(CreditCard creditCard) {
        CreditCardResponse response = new CreditCardResponse();
        response.setId(creditCard.getId());
        response.setCardType(creditCard.getCardType());
        response.setLastFourDigits(creditCard.getLastFourDigits());
        response.setCardHolderName(creditCard.getCardHolderName());
        response.setExpiryDate(creditCard.getExpiryDate());
        response.setIsDefault(creditCard.getIsDefault());
        response.setCreateTime(creditCard.getCreateTime());
        response.setMaskedCardNumber(CreditCardUtils.buildMaskedDisplay(
                creditCard.getFirstSixDigits(), creditCard.getLastFourDigits()));
        return response;
    }
}
