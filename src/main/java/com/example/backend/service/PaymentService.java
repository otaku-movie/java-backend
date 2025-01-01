package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.mapper.PaymentMethodMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends ServiceImpl<PaymentMethodMapper, PaymentMethod> {
 public void PayPay() {

 }
  public void PayPal() {

  }
  public void WechatPay() {

  }
  public void AliPay() {
  }
  public void CreditCardPay() {
    // 信用卡支付
  }
  public  void pay () {

  }
}
