package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
@RequestMapping("/api/creditCard")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private MessageUtils messageUtils;

    /**
     * 获取用户信用卡列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public RestBean<List<CreditCardResponse>> getCreditCardList() {
        try {
            List<CreditCardResponse> creditCards = creditCardService.getUserCreditCards();
            return RestBean.success(creditCards, messageUtils.getMessage("success.get"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 根据ID获取信用卡详情
     */
    @SaCheckLogin
    @GetMapping("/detail")
    public RestBean<CreditCard> getCreditCardDetail(@RequestParam("id") Integer id) {
        try {
            CreditCard creditCard = creditCardService.getUserCreditCard(id);
            if (creditCard == null) {
                return RestBean.error(ResponseCode.ERROR.getCode(), "信用卡不存在或无权限访问");
            }
            return RestBean.success(creditCard, messageUtils.getMessage("success.get"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 保存信用卡
     */
    @SaCheckLogin
    @PostMapping("/save")
    public RestBean<CreditCardResponse> saveCreditCard(@RequestBody @Validated CreditCardSaveQuery query) {
        try {
            CreditCardResponse creditCard = creditCardService.saveCreditCard(query);
            return RestBean.success(creditCard, messageUtils.getMessage("success.save"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 更新信用卡信息
     */
    @SaCheckLogin
    @PostMapping("/update")
    public RestBean<Void> updateCreditCard(@RequestBody @Validated CreditCardUpdateQuery query) {
        try {
            creditCardService.updateCreditCard(query);
            return RestBean.success(null, messageUtils.getMessage("success.save"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 删除信用卡
     */
    @SaCheckLogin
    @DeleteMapping("/delete")
    public RestBean<Void> deleteCreditCard(@RequestParam("id") Integer id) {
        try {
            creditCardService.deleteCreditCard(id);
            return RestBean.success(null, messageUtils.getMessage("success.remove"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 设置默认信用卡
     */
    @SaCheckLogin
    @PostMapping("/setDefault")
    public RestBean<Void> setDefaultCard(@RequestBody SetDefaultCardQuery query) {
        try {
            creditCardService.setDefaultCard(query.getId());
            return RestBean.success(null, messageUtils.getMessage("success.save"));
        } catch (Exception e) {
            return RestBean.error(ResponseCode.ERROR.getCode(), e.getMessage());
        }
    }

    @Data
    static class SetDefaultCardQuery {
        private Integer id;
    }
}
