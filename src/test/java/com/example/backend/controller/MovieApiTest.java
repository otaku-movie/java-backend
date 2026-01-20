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
 * 电影相关接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>电影列表接口</li>
 *   <li>电影详情接口</li>
 *   <li>电影角色接口</li>
 *   <li>电影版本列表接口</li>
 *   <li>电影演职员接口</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("电影接口测试")
public class MovieApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";

    /**
     * 测试电影列表接口
     */
    @Test
    @DisplayName("测试电影列表接口 - POST /api/movie/list")
    public void testMovieList() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/movie/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影详情接口
     */
    @Test
    @DisplayName("测试电影详情接口 - GET /api/movie/detail")
    public void testMovieDetail() throws Exception {
        // 使用一个存在的电影ID进行测试（根据实际情况修改）
        Integer movieId = 1;

        MvcResult result = mockMvc.perform(get(BASE_URL + "/movie/detail")
                        .param("id", movieId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(movieId))
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影详情接口 - 参数错误
     */
    @Test
    @DisplayName("测试电影详情接口 - 参数错误")
    public void testMovieDetailWithInvalidParam() throws Exception {
        mockMvc.perform(get(BASE_URL + "/movie/detail")
                        .param("id", "null")) // 无效参数
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    /**
     * 测试电影角色接口
     */
    @Test
    @DisplayName("测试电影角色接口 - GET /api/movie/character")
    public void testMovieCharacter() throws Exception {
        // 使用一个存在的电影ID进行测试
        Integer movieId = 1;

        MvcResult result = mockMvc.perform(get(BASE_URL + "/movie/character")
                        .param("id", movieId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影版本列表接口
     */
    @Test
    @DisplayName("测试电影版本列表接口 - GET /api/movie/version/list")
    public void testMovieVersionList() throws Exception {
        // 使用一个存在的电影ID进行测试
        Integer movieId = 34860; // 根据实际情况修改

        MvcResult result = mockMvc.perform(get(BASE_URL + "/movie/version/list")
                        .param("movieId", movieId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影版本列表接口 - 参数错误
     */
    @Test
    @DisplayName("测试电影版本列表接口 - 参数错误")
    public void testMovieVersionListWithInvalidParam() throws Exception {
        mockMvc.perform(get(BASE_URL + "/movie/version/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    /**
     * 测试电影演职员接口
     */
    @Test
    @DisplayName("测试电影演职员接口 - GET /api/movie/staff")
    public void testMovieStaff() throws Exception {
        // 使用一个存在的电影ID进行测试
        Integer movieId = 1;

        MvcResult result = mockMvc.perform(get(BASE_URL + "/movie/staff")
                        .param("id", movieId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试电影规格接口
     */
    @Test
    @DisplayName("测试电影规格接口 - GET /api/movie/spec")
    public void testMovieSpec() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/movie/spec"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }
}
