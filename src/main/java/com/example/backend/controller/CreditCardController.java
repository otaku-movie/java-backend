package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.backend.constants.ApiPaths;
import com.example.backend.constants.MessageKeys;
import com.example.backend.entity.CreditCard;
import com.example.backend.entity.RestBean;
import com.example.backend.enumerate.ResponseCode;
import com.example.backend.query.CreditCardSaveQuery;
import com.example.backend.query.CreditCardUpdateQuery;
import com.example.backend.response.CreditCardResponse;
import com.example.backend.service.CreditCardService;
import com.example.backend.utils.MessageUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private MessageUtils messageUtils;

    /**
     * 获取用户信用卡列表
     */
    @SaCheckLogin
    @GetMapping(ApiPaths.Common.CreditCard.LIST)
    public RestBean<List<CreditCardResponse>> getCreditCardList() {
        try {
            List<CreditCardResponse> creditCards = creditCardService.getUserCreditCards();
            return RestBean.success(creditCards, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 根据ID获取信用卡详情（仅返回遮罩卡号，符合 PCI DSS）
     */
    @SaCheckLogin
    @GetMapping(ApiPaths.Common.CreditCard.DETAIL)
    public RestBean<CreditCardResponse> getCreditCardDetail(@RequestParam("id") Integer id) {
        try {
            CreditCardResponse response = creditCardService.getCreditCardResponse(id);
            if (response == null) {
                return RestBean.error(ResponseCode.ERROR.getCode(), MessageUtils.getMessage(MessageKeys.Error.NOT_PERMISSION));
            }
            return RestBean.success(response, MessageUtils.getMessage(MessageKeys.Admin.GET_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 保存信用卡
     */
    @SaCheckLogin
    @PostMapping(ApiPaths.Common.CreditCard.SAVE)
    public RestBean<CreditCardResponse> saveCreditCard(@RequestBody @Validated CreditCardSaveQuery query) {
        try {
            CreditCardResponse creditCard = creditCardService.saveCreditCard(query);
            return RestBean.success(creditCard, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 更新信用卡信息
     */
    @SaCheckLogin
    @PostMapping(ApiPaths.Common.CreditCard.UPDATE)
    public RestBean<Void> updateCreditCard(@RequestBody @Validated CreditCardUpdateQuery query) {
        try {
            creditCardService.updateCreditCard(query);
            return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 删除信用卡
     */
    @SaCheckLogin
    @DeleteMapping(ApiPaths.Common.CreditCard.DELETE)
    public RestBean<Void> deleteCreditCard(@RequestParam("id") Integer id) {
        try {
            creditCardService.deleteCreditCard(id);
            return RestBean.success(null, MessageUtils.getMessage(MessageKeys.Admin.Movie.REMOVE_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 设置默认信用卡
     */
    @SaCheckLogin
    @PostMapping(ApiPaths.Common.CreditCard.SET_DEFAULT)
    public RestBean<Void> setDefaultCard(@RequestBody SetDefaultCardQuery query) {
        try {
            creditCardService.setDefaultCard(query.getId());
            return RestBean.success(null, messageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    @Data
    static class SetDefaultCardQuery {
        private Integer id;
    }
}
