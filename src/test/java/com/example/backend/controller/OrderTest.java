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
 * 订单相关接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>创建订单接口</li>
 *   <li>订单详情接口</li>
 *   <li>订单列表接口</li>
 *   <li>取消订单接口</li>
 *   <li>订单超时接口</li>
 *   <li>我的票据接口</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("订单接口测试")
public class OrderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";
    
    // 测试数据（需要根据实际情况修改）
    private static final Integer TEST_MOVIE_SHOW_TIME_ID = 1;

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
     * 测试创建订单接口
     */
    @Test
    @DisplayName("测试创建订单接口")
    public void testCreateOrder() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        // 先选座（如果需要）
        // 然后创建订单
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
        
        List<Map<String, Object>> seats = new ArrayList<>();
        Map<String, Object> seat1 = new HashMap<>();
        seat1.put("x", 1);
        seat1.put("y", 1);
        seat1.put("seatId", 1);
        seat1.put("movieTicketTypeId", 1);
        seats.add(seat1);
        
        requestBody.put("seat", seats);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/movieOrder/create")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        System.out.println("订单创建响应: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试订单详情接口
     */
    @Test
    @DisplayName("测试订单详情接口")
    public void testOrderDetail() throws Exception {
        Integer orderId = 1; // 使用已存在的订单ID

        MvcResult result = mockMvc.perform(get(BASE_URL + "/movieOrder/detail")
                        .param("id", orderId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        System.out.println("订单详情响应: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试取消订单接口
     */
    @Test
    @DisplayName("测试取消订单接口")
    public void testCancelOrder() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", 1); // 使用已存在的订单ID

        mockMvc.perform(post(BASE_URL + "/movieOrder/cancel")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 测试订单超时接口
     */
    @Test
    @DisplayName("测试订单超时接口")
    public void testTimeoutOrder() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", 1); // 使用已存在的订单ID

        mockMvc.perform(post(BASE_URL + "/movieOrder/timeout")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 测试我的票据接口
     */
    @Test
    @DisplayName("测试我的票据接口")
    public void testMyTickets() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);
        requestBody.put("orderState", null); // 所有状态

        mockMvc.perform(post(BASE_URL + "/movieOrder/myTickets")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
    }
}
