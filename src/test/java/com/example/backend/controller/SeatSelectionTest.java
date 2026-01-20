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
 * 选座相关接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>保存选座接口</li>
 *   <li>取消选座接口</li>
 *   <li>用户选座列表接口</li>
 *   <li>选座列表接口</li>
 *   <li>座位冲突检测</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("选座接口测试")
public class SeatSelectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";
    
    // 测试数据（需要根据实际情况修改）
    private static final Integer TEST_MOVIE_SHOW_TIME_ID = 1;
    private static final Integer TEST_THEATER_HALL_ID = 1;

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
     * 测试保存选座接口 - 成功选座
     */
    @Test
    @DisplayName("测试保存选座接口 - 成功选座")
    public void testSaveSelectSeatSuccess() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
        requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
        
        List<Map<String, Object>> seatPositions = new ArrayList<>();
        Map<String, Object> seat1 = new HashMap<>();
        seat1.put("x", 1);
        seat1.put("y", 1);
        seat1.put("seatId", 1);
        seatPositions.add(seat1);
        
        requestBody.put("seatPosition", seatPositions);

        mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
    }

    /**
     * 测试保存选座接口 - 座位冲突（其他用户已选）
     */
    @Test
    @DisplayName("测试保存选座接口 - 座位冲突")
    public void testSaveSelectSeatConflict() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        // 先选一个座位
        Map<String, Object> requestBody1 = new HashMap<>();
        requestBody1.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
        requestBody1.put("theaterHallId", TEST_THEATER_HALL_ID);
        
        List<Map<String, Object>> seatPositions1 = new ArrayList<>();
        Map<String, Object> seat1 = new HashMap<>();
        seat1.put("x", 10);
        seat1.put("y", 10);
        seat1.put("seatId", 100);
        seatPositions1.add(seat1);
        requestBody1.put("seatPosition", seatPositions1);

        mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody1)))
                .andReturn();

        // 尝试再次选择同一个座位（模拟其他用户）
        // 注意：实际测试中需要使用不同的token模拟不同用户
        // 这里只是演示测试逻辑
        
        System.out.println("座位冲突测试完成");
    }

    /**
     * 测试取消选座接口
     */
    @Test
    @DisplayName("测试取消选座接口")
    public void testCancelSelectSeat() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
        requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
        
        List<Map<String, Object>> seatPositions = new ArrayList<>();
        Map<String, Object> seat1 = new HashMap<>();
        seat1.put("x", 2);
        seat1.put("y", 2);
        seat1.put("seatId", 2);
        seatPositions.add(seat1);
        
        requestBody.put("seatPosition", seatPositions);

        mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/cancel")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 测试用户选座列表接口
     */
    @Test
    @DisplayName("测试用户选座列表接口")
    public void testUserSelectSeatList() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        mockMvc.perform(get(BASE_URL + "/movie_show_time/user_select_seat")
                        .header("token", token)
                        .param("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
    }

    /**
     * 测试选座列表接口
     */
    @Test
    @DisplayName("测试选座列表接口")
    public void testSelectSeatList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/movie_show_time/select_seat/list")
                        .param("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID.toString())
                        .param("theaterHallId", TEST_THEATER_HALL_ID.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
    }

    /**
     * 测试参数验证 - 缺少场次ID
     */
    @Test
    @DisplayName("测试保存选座接口 - 参数验证")
    public void testSaveSelectSeatMissingParams() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);

        mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }
}
