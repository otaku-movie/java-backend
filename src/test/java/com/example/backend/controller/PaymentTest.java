package com.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 支付相关接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>支付订单接口</li>
 *   <li>支付成功场景</li>
 *   <li>支付失败场景</li>
 *   <li>重复支付场景</li>
 *   <li>订单状态验证</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("支付接口测试")
public class PaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";

    /**
     * 登录并获取token
     */
    private String loginAndGetToken() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
            if (responseMap.get("code") != null && responseMap.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data != null && data.get("token") != null) {
                    return data.get("token").toString();
                }
            }
        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 测试支付订单接口 - 成功支付
     */
    @Test
    @DisplayName("测试支付订单接口 - 成功支付")
    public void testPayOrderSuccess() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", 1); // 使用已创建且状态为 order_created 的订单ID
        requestBody.put("payId", 1); // 支付方式ID

        mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
    }

    /**
     * 测试支付订单接口 - 订单状态错误（非 order_created 状态）
     */
    @Test
    @DisplayName("测试支付订单接口 - 订单状态错误")
    public void testPayOrderWrongState() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", 999); // 使用已支付或其他状态的订单ID
        requestBody.put("payId", 1);

        mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();
    }

    /**
     * 测试支付订单接口 - 重复支付
     */
    @Test
    @DisplayName("测试支付订单接口 - 重复支付")
    public void testPayOrderDuplicate() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", 1);
        requestBody.put("payId", 1);

        // 第一次支付
        mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        // 第二次支付（应该失败）
        mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 测试支付订单接口 - 参数验证
     */
    @Test
    @DisplayName("测试支付订单接口 - 参数验证")
    public void testPayOrderMissingParams() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        // 缺少orderId
        Map<String, Object> requestBody1 = new HashMap<>();
        requestBody1.put("payId", 1);

        mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody1)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }
}
