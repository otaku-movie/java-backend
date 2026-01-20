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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * App 端电影相关接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>正在热映接口</li>
 *   <li>即将上映接口</li>
 *   <li>电影场次接口</li>
 *   <li>电影演职员接口</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("App 端电影接口测试")
public class AppMovieApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String APP_BASE_URL = "/api/app";

    /**
     * 测试正在热映接口
     */
    @Test
    @DisplayName("测试正在热映接口 - GET /api/app/movie/nowShowing")
    public void testNowShowing() throws Exception {
        MvcResult result = mockMvc.perform(get(APP_BASE_URL + "/movie/nowShowing")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试即将上映接口
     */
    @Test
    @DisplayName("测试即将上映接口 - GET /api/app/movie/comingSoon")
    public void testComingSoon() throws Exception {
        MvcResult result = mockMvc.perform(get(APP_BASE_URL + "/movie/comingSoon")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影场次接口 - 基本查询
     */
    @Test
    @DisplayName("测试电影场次接口 - POST /api/app/movie/showTime")
    public void testMovieShowTime() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieId", 1);
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);

        MvcResult result = mockMvc.perform(post(APP_BASE_URL + "/movie/showTime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影场次接口 - 带筛选条件
     */
    @Test
    @DisplayName("测试电影场次接口 - 带筛选条件")
    public void testMovieShowTimeWithFilters() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieId", 1);
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);
        requestBody.put("versionCode", 1); // 版本筛选
        requestBody.put("subtitleId", 1); // 字幕筛选
        requestBody.put("keyword", "影院名称"); // 搜索关键词
        requestBody.put("startTimeFrom", "2025-01-15 09:00"); // 开始时间
        requestBody.put("startTimeTo", "2025-01-15 23:00"); // 结束时间
        requestBody.put("use30HourFormat", false); // 使用24小时制

        MvcResult result = mockMvc.perform(post(APP_BASE_URL + "/movie/showTime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影场次接口 - 30小时制时间格式
     */
    @Test
    @DisplayName("测试电影场次接口 - 30小时制时间格式")
    public void testMovieShowTimeWith30HourFormat() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("movieId", 1);
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);
        requestBody.put("startTimeFrom", "2025-01-15 25:00"); // 30小时制：25:00 表示第二天的 01:00
        requestBody.put("startTimeTo", "2025-01-15 29:00"); // 30小时制：29:00 表示第二天的 05:00
        requestBody.put("use30HourFormat", true);

        MvcResult result = mockMvc.perform(post(APP_BASE_URL + "/movie/showTime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影演职员接口
     */
    @Test
    @DisplayName("测试电影演职员接口 - GET /api/app/movie/staff")
    public void testAppMovieStaff() throws Exception {
        MvcResult result = mockMvc.perform(get(APP_BASE_URL + "/movie/staff")
                        .param("movieId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }
}
